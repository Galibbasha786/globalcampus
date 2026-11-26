package com.globalcampus.support.ui.call

import android.Manifest
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import android.view.SurfaceView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.globalcampus.support.BuildConfig
import com.globalcampus.support.data.model.CallHistoryItem
import com.globalcampus.support.data.repository.AuthRepository
import com.globalcampus.support.data.repository.CallHistoryRepository
import com.globalcampus.support.data.repository.ChatRepository
import com.globalcampus.support.ui.components.StudentAvatar
import com.globalcampus.support.ui.theme.BackgroundDark
import com.globalcampus.support.ui.theme.SurfaceDark
import com.globalcampus.support.ui.theme.AccentGreen
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCallScreen(
    recipientName: String = "LPU Global Community",
    isGroupCall: Boolean = true,
    groupId: String = "global",
    isJoining: Boolean = false,
    onEndCall: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val callRepo = remember { CallHistoryRepository() }
    val chatRepo = remember { ChatRepository() }
    val authRepo = remember { AuthRepository() }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    var isMuted by remember { mutableStateOf(false) }
    var isVideoOff by remember { mutableStateOf(false) }
    var callSeconds by remember { mutableIntStateOf(0) }
    val remoteUids = remember { mutableStateListOf<Int>() }
    var myJoinedUid by remember { mutableIntStateOf(0) }
    var rtcEngine by remember { mutableStateOf<RtcEngine?>(null) }
    var hasPermissions by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var callStatus by remember { mutableStateOf("Connecting to channel...") }
    var agoraError by remember { mutableStateOf<Int?>(null) }

    var toneGenerator by remember { mutableStateOf<ToneGenerator?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasPermissions = perms[Manifest.permission.CAMERA] == true &&
                perms[Manifest.permission.RECORD_AUDIO] == true
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        )
    }

    fun joinAgoraChannel(engine: RtcEngine, token: String?) {
        try {
            engine.enableVideo()
            engine.enableAudio()
            engine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
            engine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
            engine.setVideoEncoderConfiguration(
                VideoEncoderConfiguration(
                    VideoEncoderConfiguration.VD_640x360,
                    VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                    VideoEncoderConfiguration.STANDARD_BITRATE,
                    VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
                )
            )
            engine.startPreview()
            engine.muteLocalAudioStream(false)
            engine.muteLocalVideoStream(false)

            val channelName = BuildConfig.AGORA_CHANNEL_NAME.ifBlank { "Test" }
            val rawToken = token?.trim()?.ifBlank { null }
            android.util.Log.d("AgoraRTC", "Joining channel '$channelName' with token: ${rawToken?.take(15)}...")
            engine.joinChannel(rawToken, channelName, "", 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LaunchedEffect(hasPermissions) {
        if (hasPermissions) {
            try {
                // Play short ringtone ONLY when initiating new call, NOT when joining existing call
                if (!isJoining) {
                    toneGenerator = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 60).apply {
                        startTone(ToneGenerator.TONE_SUP_RINGTONE, 800)
                    }
                }

                val appId = BuildConfig.AGORA_APP_ID.ifBlank { "88ed4ecb11294c9497197ca28a965e75" }
                val config = RtcEngineConfig().apply {
                    mContext = context
                    mAppId = appId
                    mEventHandler = object : IRtcEngineEventHandler() {
                        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
                            mainHandler.post {
                                myJoinedUid = uid
                                callStatus = "Live HD • In #$channel (UID: $uid)"
                                agoraError = null
                            }
                            android.util.Log.d("AgoraRTC", "onJoinChannelSuccess: channel=$channel, uid=$uid")
                        }

                        override fun onUserJoined(uid: Int, elapsed: Int) {
                            mainHandler.post {
                                if (!remoteUids.contains(uid)) {
                                    remoteUids.add(uid)
                                }
                                toneGenerator?.stopTone()
                                callStatus = "Connected with Participant #$uid"
                            }
                            android.util.Log.d("AgoraRTC", "onUserJoined: uid=$uid")
                        }

                        override fun onUserOffline(uid: Int, reason: Int) {
                            mainHandler.post {
                                remoteUids.remove(uid)
                                callStatus = "Participant #$uid went offline"
                            }
                            android.util.Log.d("AgoraRTC", "onUserOffline: uid=$uid, reason=$reason")
                        }

                        override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
                            mainHandler.post {
                                if (state == Constants.REMOTE_VIDEO_STATE_DECODING || state == Constants.REMOTE_VIDEO_STATE_STARTING) {
                                    if (!remoteUids.contains(uid)) {
                                        remoteUids.add(uid)
                                    }
                                    toneGenerator?.stopTone()
                                }
                            }
                            android.util.Log.d("AgoraRTC", "onRemoteVideoStateChanged: uid=$uid, state=$state, reason=$reason")
                        }

                        override fun onError(err: Int) {
                            mainHandler.post {
                                agoraError = err
                                callStatus = when (err) {
                                    109 -> "Token Expired (Code 109). Generate new Temp Token"
                                    110 -> "Token/Channel Mismatch (Code 110). Channel must be '${BuildConfig.AGORA_CHANNEL_NAME}'"
                                    101 -> "Invalid App ID (Code 101)"
                                    else -> "Agora Error Code $err"
                                }
                            }
                            android.util.Log.e("AgoraRTC", "onError: $err")
                        }

                        override fun onTokenPrivilegeWillExpire(token: String?) {
                            mainHandler.post {
                                callStatus = "Warning: Agora Token will expire soon"
                            }
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
                            mainHandler.post {
                                if (!remoteUids.contains(uid)) {
                                    remoteUids.add(uid)
                                }
                                toneGenerator?.stopTone()
                            }
                        }
                    }
                }

                val engine = RtcEngine.create(config)
                joinAgoraChannel(engine, BuildConfig.AGORA_TEMP_TOKEN)
                rtcEngine = engine
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            toneGenerator?.stopTone()
            toneGenerator?.release()
            rtcEngine?.leaveChannel()
            RtcEngine.destroy()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            callSeconds += 1
        }
    }

    val minutes = callSeconds / 60
    val seconds = callSeconds % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)

    fun handleCallEnd() {
        toneGenerator?.stopTone()
        rtcEngine?.leaveChannel()
        scope.launch {
            callRepo.recordCall(recipientName, isGroupCall, callSeconds)
            val durationText = "${callSeconds / 60}m ${callSeconds % 60}s"
            chatRepo.sendMessage(groupId, "📹 Video Call Ended • Duration: $durationText")
            
            // ALWAYS reset active call banner in Firestore when call ends
            chatRepo.endGroupCall(groupId)
            onEndCall()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Call Top Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        if (isGroupCall) "Group Video Call • $recipientName" else "Video Call with $recipientName",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                    val statusColor = if (agoraError != null) Color(0xFFEF4444) else AccentGreen
                    Text(
                        "$callStatus • $timeString",
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showHistoryDialog = true }) {
                        Icon(Icons.Default.History, "Call History", tint = Color.White)
                    }

                    Surface(
                        color = if (agoraError != null) Color(0xFFEF4444).copy(0.2f) else Color.White.copy(0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (agoraError != null) Color(0xFFEF4444) else AccentGreen))
                            Spacer(Modifier.width(6.dp))
                            val chan = BuildConfig.AGORA_CHANNEL_NAME.ifBlank { "Test" }
                            Text("#$chan ✅", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Video Feeds Display Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (remoteUids.isNotEmpty()) {
                    // Real Agora Remote Users Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(if (remoteUids.size == 1) 1 else 2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(remoteUids, key = { it }) { uid ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF1E293B))
                            ) {
                                AndroidView(
                                    factory = { ctx ->
                                        SurfaceView(ctx).apply {
                                            setZOrderMediaOverlay(false)
                                            rtcEngine?.setupRemoteVideo(VideoCanvas(this, VideoCanvas.RENDER_MODE_FIT, uid))
                                        }
                                    },
                                    update = { surfaceView ->
                                        rtcEngine?.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid))
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                                Surface(
                                    color = Color.Black.copy(0.6f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
                                ) {
                                    Text("Participant #$uid", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                        }
                    }
                } else {
                    // Full Screen Active Participant View
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            StudentAvatar(
                                avatarUrl = "",
                                name = recipientName,
                                size = 110.dp,
                                showOnlineStatus = true
                            )
                            Spacer(Modifier.height(18.dp))
                            Text(
                                recipientName,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Connected • Encrypted Video Channel",
                                    color = Color(0xFF10B981),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Surface(
                            color = Color.Black.copy(0.4f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Videocam, null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Remote Video Active", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Local Self Camera Preview Box (PIP)
                if (!isVideoOff && hasPermissions) {
                    Box(
                        modifier = Modifier
                            .size(width = 110.dp, height = 150.dp)
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black)
                            .border(2.dp, Color(0xFF10B981), RoundedCornerShape(16.dp))
                    ) {
                        if (rtcEngine != null) {
                            AndroidView(
                                factory = { ctx ->
                                    SurfaceView(ctx).apply {
                                        setZOrderMediaOverlay(true)
                                        rtcEngine?.setupLocalVideo(VideoCanvas(this, VideoCanvas.RENDER_MODE_HIDDEN, 0))
                                    }
                                },
                                update = { surfaceView ->
                                    rtcEngine?.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Self View", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Bottom Action Controls Bar
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            isMuted = !isMuted
                            rtcEngine?.muteLocalAudioStream(isMuted)
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (isMuted) Color.Red else Color.White.copy(0.15f))
                    ) {
                        Icon(if (isMuted) Icons.Default.MicOff else Icons.Default.Mic, "Mute", tint = Color.White)
                    }

                    IconButton(
                        onClick = {
                            isVideoOff = !isVideoOff
                            rtcEngine?.muteLocalVideoStream(isVideoOff)
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (isVideoOff) Color.Red else Color.White.copy(0.15f))
                    ) {
                        Icon(if (isVideoOff) Icons.Default.VideocamOff else Icons.Default.Videocam, "Video", tint = Color.White)
                    }

                    IconButton(
                        onClick = { rtcEngine?.switchCamera() },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(0.15f))
                    ) {
                        Icon(Icons.Default.Cameraswitch, "Flip Camera", tint = Color.White)
                    }

                    IconButton(
                        onClick = { handleCallEnd() },
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    ) {
                        Icon(Icons.Default.CallEnd, "End Call", tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                }
            }
        }
    }

    if (showHistoryDialog) {
        CallHistoryModalDialog(onDismiss = { showHistoryDialog = false })
    }
}

@Composable
fun CallHistoryModalDialog(onDismiss: () -> Unit) {
    val callRepo = remember { CallHistoryRepository() }
    var historyItems by remember { mutableStateOf<List<CallHistoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        callRepo.getCallHistory().onSuccess {
            historyItems = it
            isLoading = false
        }
    }

    val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, null, tint = Color(0xFF10B981))
                Spacer(Modifier.width(8.dp))
                Text("Video Call History Log", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            if (isLoading) {
                Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF10B981))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(historyItems, key = { it.id }) { item ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (item.isGroupCall) Icons.Default.Groups else Icons.Default.VideoCall,
                                    null,
                                    tint = Color(0xFF10B981)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(item.recipientName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(formatter.format(Date(item.timestamp)), fontSize = 11.sp, color = Color.Gray)
                                }
                                Text(
                                    "${item.durationSeconds / 60}m ${item.durationSeconds % 60}s",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", fontWeight = FontWeight.Bold) }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
