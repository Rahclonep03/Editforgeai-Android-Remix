package com.example.editforge.ui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.editforge.data.audio.CyberneticAudioEngine
import com.example.editforge.data.firebase.AuthResult
import com.example.editforge.data.firebase.FirebaseAuthService
import com.example.editforge.data.firebase.FirebaseFirestoreService
import com.example.editforge.data.firebase.UserProfileData
import com.example.editforge.data.local.EditForgeDatabase
import com.example.editforge.data.model.*
import com.example.editforge.data.paddle.BillingCycle
import com.example.editforge.data.paddle.PaddleBillingService
import com.example.editforge.data.paddle.PaddleSubscriptionInfo
import com.example.editforge.data.repository.EditForgeRepository
import com.example.editforge.data.supabase.SupabaseConfig
import com.example.editforge.data.supabase.SupabaseLovableService
import com.example.editforge.ui.theme.StudioThemePalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditForgeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = EditForgeDatabase.getInstance(application)
    private val repository = EditForgeRepository(database)

    private val appPrefs = application.getSharedPreferences("editforge_app_prefs", Context.MODE_PRIVATE)

    val authService = FirebaseAuthService(application)
    val firestoreService = FirebaseFirestoreService()
    val paddleService = PaddleBillingService()

    val paddleSubscription: StateFlow<PaddleSubscriptionInfo> = paddleService.subscriptionInfo

    val isSubscriber: Boolean
        get() = paddleService.subscriptionInfo.value.tier != SubscriptionTier.FREE

    // Dynamic Theme Palette selection - Subscriber-Only Perk!
    // Free users are stuck with the first default theme (CYBER_FORGE).
    private val _currentThemePalette = MutableStateFlow(
        if (paddleService.subscriptionInfo.value.tier == SubscriptionTier.FREE) {
            StudioThemePalette.CYBER_FORGE
        } else {
            StudioThemePalette.entries.find { it.id == appPrefs.getString("selected_palette", StudioThemePalette.CYBER_FORGE.id) }
                ?: StudioThemePalette.CYBER_FORGE
        }
    )
    val currentThemePalette: StateFlow<StudioThemePalette> = _currentThemePalette.asStateFlow()

    private val _themeUpgradePrompt = MutableStateFlow<String?>(null)
    val themeUpgradePrompt: StateFlow<String?> = _themeUpgradePrompt.asStateFlow()

    fun clearThemeUpgradePrompt() {
        _themeUpgradePrompt.value = null
    }

    fun isThemeSelectionAllowed(): Boolean {
        return paddleService.subscriptionInfo.value.tier != SubscriptionTier.FREE
    }

    fun selectThemePalette(palette: StudioThemePalette): Boolean {
        val currentTier = paddleService.subscriptionInfo.value.tier
        if (currentTier == SubscriptionTier.FREE && palette != StudioThemePalette.CYBER_FORGE) {
            _themeUpgradePrompt.value = "Changing themes is an exclusive Subscriber-only perk! Free tier uses Cyber Forge. Upgrade to Plus, Pro, or Premier to personalize your studio with custom themes."
            return false
        }
        _currentThemePalette.value = palette
        appPrefs.edit().putString("selected_palette", palette.id).apply()
        return true
    }

    // Supabase & Lovable MCP Integration
    val supabaseService = SupabaseLovableService(application)
    val supabaseConfig: StateFlow<SupabaseConfig> = supabaseService.configState
    val supabaseLogs: StateFlow<List<String>> = supabaseService.syncLog

    fun updateSupabaseConfig(url: String, key: String, token: String) {
        supabaseService.saveConfig(url, key, token)
    }

    fun loadSupabaseDemoPreset() {
        supabaseService.setQuickPresetDemo()
    }

    fun testSupabaseConnection(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = supabaseService.testConnection()
            onResult(res.first, res.second)
        }
    }

    val currentUserState: StateFlow<UserProfileData?> = authService.currentUserState
    val authError: StateFlow<String?> = authService.authError
    val isAuthLoading: StateFlow<Boolean> = authService.isLoading

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    private val _isFetchingFirestore = MutableStateFlow(false)
    val isFetchingFirestore: StateFlow<Boolean> = _isFetchingFirestore.asStateFlow()

    private val _firestoreLastSyncTime = MutableStateFlow<Long?>(null)
    val firestoreLastSyncTime: StateFlow<Long?> = _firestoreLastSyncTime.asStateFlow()

    private val _firestoreProjectCount = MutableStateFlow(0)
    val firestoreProjectCount: StateFlow<Int> = _firestoreProjectCount.asStateFlow()

    private val _firestoreConnected = MutableStateFlow(true)
    val firestoreConnected: StateFlow<Boolean> = _firestoreConnected.asStateFlow()

    private val _cloudSyncStateText = MutableStateFlow("Firestore Connected")
    val cloudSyncStateText: StateFlow<String> = _cloudSyncStateText.asStateFlow()

    val projects: StateFlow<List<Project>> = repository.projectsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExports: StateFlow<List<ExportItem>> = repository.allExportsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<CreditTransaction>> = repository.transactionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _creditBalance = MutableStateFlow(44)
    val creditBalance: StateFlow<Int> = _creditBalance.asStateFlow()

    private val _activeProjectId = MutableStateFlow<String?>("demo-project-neon-horizons")
    val activeProjectId: StateFlow<String?> = _activeProjectId.asStateFlow()

    val activeProject: StateFlow<Project?> = _activeProjectId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.observeProject(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeAnalysis: StateFlow<AnalysisData?> = _activeProjectId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.observeAnalysis(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeExports: StateFlow<List<ExportItem>> = _activeProjectId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.observeExports(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Analysis workflow state
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisProgress = MutableStateFlow(0f)
    val analysisProgress: StateFlow<Float> = _analysisProgress.asStateFlow()

    private val _analysisStageText = MutableStateFlow("")
    val analysisStageText: StateFlow<String> = _analysisStageText.asStateFlow()

    // Interactive audio playback state
    private val _playingId = MutableStateFlow<String?>("demo-project-neon-horizons")
    val playingId: StateFlow<String?> = _playingId.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0.24f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private var playbackJob: Job? = null
    private var currentTrackDuration: Float = 214.5f

    init {
        // Sync balance from latest transaction
        viewModelScope.launch {
            transactions.collect { list ->
                if (list.isNotEmpty()) {
                    _creditBalance.value = list.first().balanceAfter
                }
            }
        }

        // Initial fetch from Firestore
        fetchActiveProjectsFromFirestore()

        // Real-time Firestore sync & user profile backup
        viewModelScope.launch {
            currentUserState.collect { user ->
                if (user != null) {
                    firestoreService.syncUserProfile(
                        userId = user.uid,
                        creditBalance = _creditBalance.value,
                        email = user.email,
                        displayName = user.displayName
                    )
                    fetchActiveProjectsFromFirestore()
                }
            }
        }
    }

    fun fetchActiveProjectsFromFirestore(force: Boolean = false) {
        viewModelScope.launch {
            val uid = authService.currentUserId
            _isFetchingFirestore.value = true
            _cloudSyncStateText.value = "Fetching from Firestore..."
            try {
                val cloudProjects = firestoreService.fetchCloudProjects(uid)
                if (cloudProjects.isNotEmpty()) {
                    repository.insertOrUpdateProjects(cloudProjects)
                    _firestoreProjectCount.value = cloudProjects.size
                    _firestoreLastSyncTime.value = System.currentTimeMillis()
                    _cloudSyncStateText.value = "Firestore Synced (${cloudProjects.size} tracks)"
                    _firestoreConnected.value = true
                }
            } catch (e: Exception) {
                Log.w("EditForgeViewModel", "Firestore fetch error, fallback active: ${e.message}")
                _cloudSyncStateText.value = "Offline Cache Active"
            } finally {
                _isFetchingFirestore.value = false
            }
        }
    }

    fun createCloudAudioProject(
        name: String,
        fileName: String,
        duration: Float,
        status: ProjectStatus = ProjectStatus.ANALYZING
    ) {
        viewModelScope.launch {
            val newProject = repository.createProject(name, fileName, duration)
            val uid = authService.currentUserId
            firestoreService.syncProjectToCloud(uid, newProject.copy(status = status))
            fetchActiveProjectsFromFirestore(force = true)
        }
    }

    fun signInWithGoogle(activityContext: android.content.Context? = null, email: String = "Rahclonep@gmail.com") {
        viewModelScope.launch {
            val result = authService.signInWithGoogle(activityContext, email)
            if (result is AuthResult.Success) {
                syncAllToCloudNow()
            }
        }
    }

    fun signInWithGoogleDirect(email: String = "Rahclonep@gmail.com") {
        viewModelScope.launch {
            val result = authService.signInWithGoogleDirect(email)
            if (result is AuthResult.Success) {
                syncAllToCloudNow()
            }
        }
    }

    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            val result = authService.signInWithEmail(email, pass)
            if (result is AuthResult.Success) {
                syncAllToCloudNow()
            }
        }
    }

    fun createAccountWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            val result = authService.createAccountWithEmail(email, pass)
            if (result is AuthResult.Success) {
                syncAllToCloudNow()
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authService.signOut()
        }
    }

    fun clearAuthError() {
        authService.clearError()
    }

    fun syncAllToCloudNow() {
        viewModelScope.launch {
            val uid = authService.currentUserId
            _isCloudSyncing.value = true
            _cloudSyncStateText.value = "Syncing with Firestore..."
            try {
                val user = currentUserState.value
                firestoreService.syncUserProfile(
                    userId = uid,
                    creditBalance = _creditBalance.value,
                    email = user?.email,
                    displayName = user?.displayName
                )
                projects.value.forEach { proj ->
                    firestoreService.syncProjectToCloud(uid, proj)
                }
                allExports.value.forEach { exp ->
                    firestoreService.syncExportToCloud(uid, exp)
                }
                _cloudSyncStateText.value = "Firestore Synced"
            } catch (e: Exception) {
                _cloudSyncStateText.value = "Offline Cache Active"
            } finally {
                _isCloudSyncing.value = false
            }
        }
    }

    fun selectProject(projectId: String) {
        _activeProjectId.value = projectId
        _playingId.value = projectId
        _playbackProgress.value = 0f
        _isPlaying.value = false
        playbackJob?.cancel()
    }

    fun cancelAnalysis() {
        _isAnalyzing.value = false
        _analysisProgress.value = 0f
        _analysisStageText.value = "Analysis cancelled"
    }

    fun uploadAndAnalyze(
        name: String,
        fileName: String,
        duration: Float = 195f,
        autoForgeBundle: Boolean = false,
        onComplete: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isAnalyzing.value = true
                _analysisProgress.value = 0.10f
                _analysisStageText.value = "Ingesting master audio stream ($fileName)..."

                val project = repository.createProject(name, fileName, duration)
                _activeProjectId.value = project.id

                // Fire-and-forget background cloud sync so analysis NEVER hangs on Firestore
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        firestoreService.syncProjectToCloud(authService.currentUserId, project)
                    } catch (e: Exception) {
                        Log.w("EditForgeViewModel", "Cloud sync deferred: ${e.message}")
                    }
                }

                repository.runAnalysis(project.id) { progress, stage ->
                    _analysisProgress.value = progress
                    _analysisStageText.value = stage
                }

                if (autoForgeBundle && _creditBalance.value >= 5) {
                    _analysisProgress.value = 0.90f
                    _analysisStageText.value = "Forging Core Deliverable Bundle (60s, 30s, 15s, Sting)..."
                    repository.forgeCoreBundle(project.id)
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            syncAllToCloudNow()
                        } catch (e: Exception) {
                            Log.w("EditForgeViewModel", "Cloud bundle sync deferred: ${e.message}")
                        }
                    }
                }

                _analysisProgress.value = 1.0f
                _analysisStageText.value = "Audio Analysis Complete! Opening Studio..."
                delay(300)

                _isAnalyzing.value = false
                onComplete(project.id)
            } catch (e: Exception) {
                Log.e("EditForgeViewModel", "Analysis pipeline error: ${e.message}", e)
                _isAnalyzing.value = false
                _analysisStageText.value = "Analysis error: ${e.localizedMessage}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun forgeCoreBundle(projectId: String) {
        if (_creditBalance.value < 5) return
        viewModelScope.launch {
            repository.forgeCoreBundle(projectId)
            syncAllToCloudNow()
        }
    }

    fun forgeSingleDeliverable(projectId: String, type: DeliverableType) {
        if (_creditBalance.value < type.creditCost) return
        viewModelScope.launch {
            repository.forgeSingleDeliverable(projectId, type)
            syncAllToCloudNow()
        }
    }

    fun forgeAlternative(export: ExportItem) {
        if (_creditBalance.value < 1) return
        viewModelScope.launch {
            export.projectId.let { pid ->
                repository.forgeAlternative(pid, export)
                syncAllToCloudNow()
            }
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            firestoreService.deleteProjectFromCloud(authService.currentUserId, projectId)
            if (_activeProjectId.value == projectId) {
                _activeProjectId.value = projects.value.firstOrNull { it.id != projectId }?.id
            }
        }
    }

    fun purchaseCredits(amount: Int, tierName: String) {
        viewModelScope.launch {
            repository.addCredits(amount, "Subscription Allocation", "$tierName Tier ($amount credits added)")
            firestoreService.syncUserProfile(
                userId = authService.currentUserId,
                creditBalance = _creditBalance.value + amount,
                email = currentUserState.value?.email,
                displayName = currentUserState.value?.displayName
            )
        }
    }

    fun togglePlay(id: String, duration: Float) {
        if (_playingId.value == id) {
            if (_isPlaying.value) {
                pausePlayback()
            } else {
                startPlayback(id, duration)
            }
        } else {
            _playingId.value = id
            _playbackProgress.value = 0f
            startPlayback(id, duration)
        }
    }

    fun play(id: String, duration: Float) {
        if (!_isPlaying.value || _playingId.value != id) {
            if (_playingId.value != id) {
                _playbackProgress.value = 0f
            }
            startPlayback(id, duration)
        }
    }

    fun pause() {
        pausePlayback()
    }

    fun stopPlayback() {
        _isPlaying.value = false
        _playbackProgress.value = 0f
        CyberneticAudioEngine.stop()
        playbackJob?.cancel()
    }

    fun seekTo(progress: Float) {
        val clamped = progress.coerceIn(0f, 1f)
        _playbackProgress.value = clamped
        if (_isPlaying.value) {
            CyberneticAudioEngine.start(currentTrackDuration, clamped)
        }
    }

    private fun startPlayback(id: String, duration: Float) {
        _playingId.value = id
        currentTrackDuration = duration.coerceAtLeast(1f)
        _isPlaying.value = true

        CyberneticAudioEngine.start(currentTrackDuration, _playbackProgress.value)

        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val stepSeconds = 0.05f
            val progressStep = stepSeconds / currentTrackDuration

            while (_isPlaying.value) {
                delay((stepSeconds * 1000).toLong())
                val next = _playbackProgress.value + progressStep
                if (next >= 1.0f) {
                    _playbackProgress.value = 0f
                    _isPlaying.value = false
                    CyberneticAudioEngine.stop()
                    break
                } else {
                    _playbackProgress.value = next
                }
            }
        }
    }

    private fun pausePlayback() {
        _isPlaying.value = false
        CyberneticAudioEngine.stop()
        playbackJob?.cancel()
    }

    fun upgradeWithPaddle(tier: SubscriptionTier, cycle: BillingCycle, cardLast4: String) {
        viewModelScope.launch {
            val success = paddleService.upgradeSubscription(tier, cycle, cardLast4)
            if (success) {
                repository.addCredits(
                    amount = tier.monthlyCredits,
                    reason = "Paddle Subscription (${tier.label} ${cycle.label})",
                    description = "Allocated ${tier.monthlyCredits} credits via Paddle MoR Checkout"
                )
                syncAllToCloudNow()
            }
        }
    }

    fun purchaseCreditsWithPaddle(amount: Int, packTitle: String, price: String) {
        viewModelScope.launch {
            repository.addCredits(
                amount = amount,
                reason = "Paddle Credit Pack ($packTitle)",
                description = "Purchased $amount credits ($price) via Paddle Checkout"
            )
            syncAllToCloudNow()
        }
    }

    fun cancelPaddleSubscription() {
        paddleService.cancelSubscription()
    }

    fun resumePaddleSubscription() {
        paddleService.resumeSubscription()
    }

    override fun onCleared() {
        super.onCleared()
        CyberneticAudioEngine.stop()
        playbackJob?.cancel()
    }
}
