package kr.bluevisor.openaittssample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kr.bluevisor.openaittssample.ui.theme.OpenAITtsSampleTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenAITtsSampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TextToSpeechScreen(
                        modifier = Modifier.padding(innerPadding),
                        apiKey = "OpenAiKey"
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextToSpeechScreen(
    viewModel: TextToSpeechViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    apiKey: String
) {
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    val error by viewModel.error.collectAsState(initial = null)

    var text by remember { mutableStateOf("") }
    var voiceInstruction by remember { mutableStateOf("") }
    var selectedVoice by remember { mutableStateOf("alloy") }

    val voices = listOf(
        "alloy", "ash", "coral", "echo",
        "fable", "onyx", "nova", "sage", "shimmer"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 텍스트 입력 필드
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("텍스트 입력") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        // 목소리 지시사항 입력 필드
        OutlinedTextField(
            value = voiceInstruction,
            onValueChange = { voiceInstruction = it },
            label = { Text("목소리 지시사항 (선택사항)") },
            modifier = Modifier.fillMaxWidth()
        )

        // 목소리 선택
        Text(
            text = "목소리 종류",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        // 드롭다운 메뉴
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                readOnly = true,
                value = selectedVoice,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                voices.forEach { voice ->
                    DropdownMenuItem(
                        text = { Text(voice) },
                        onClick = {
                            selectedVoice = voice
                            expanded = false
                        }
                    )
                }
            }
        }

        // 음성 생성 버튼
        Button(
            onClick = {
                val voiceInst = voiceInstruction.takeIf { it.isNotBlank() }
                viewModel.generateSpeech(apiKey, text, selectedVoice, voiceInst)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && text.isNotBlank()
        ) {
            Text("음성 생성")
        }

        // 로딩 표시
        if (isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularProgressIndicator()
            }
        }

        // 에러 메시지
        error?.let { errorMessage ->
            Text(
                text = "오류: $errorMessage",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}