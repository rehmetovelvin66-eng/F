package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.DeviceDiagnostics
import com.example.ui.theme.AmberWarm
import com.example.ui.theme.AuroraMint

@Composable
fun DiagnosticsScreen(
    diagnostics: DeviceDiagnostics?,
    onCopiedCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 620.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PhoneAndroid,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Cihaz ve HyperOS Analizi",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (diagnostics != null) {
                        DiagnosticRow("Üretici / Model", "${diagnostics.manufacturer} ${diagnostics.model}")
                        DiagnosticRow("Cihaz Kod Adı", diagnostics.deviceCodeName)
                        DiagnosticRow("Android Sürümü", "Android ${diagnostics.androidVersion} (API ${diagnostics.sdkInt})")
                        DiagnosticRow("MIUI / HyperOS", diagnostics.hyperOsVersion)
                        DiagnosticRow("RAM Durumu", "${diagnostics.availableRamGb} / ${diagnostics.totalRamGb}")
                        DiagnosticRow(
                            "Pencere Bulanıklığı API",
                            if (diagnostics.crossWindowBlurSupported) "Destekleniyor (API 31+)" else "Desteklenmiyor",
                            highlightColor = if (diagnostics.crossWindowBlurSupported) AuroraMint else AmberWarm
                        )
                        DiagnosticRow(
                            "Sistem Blur Aktifliği",
                            if (diagnostics.crossWindowBlurEnabled) "Açık (Sistem İzinli)" else "Kapalı / Üretici Kısıtlı",
                            highlightColor = if (diagnostics.crossWindowBlurEnabled) AuroraMint else AmberWarm
                        )
                        DiagnosticRow("SurfaceFlinger Blur", diagnostics.surfaceFlingerBlurProp)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 620.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Neden Advanced Texture Yok?",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Xiaomi, Redmi Note 15 4G gibi orta/giriş segment cihazlarda Sistem Arayüzü (SystemUI) performansını korumak için \"Gelişmiş Dokular\" (Advanced Textures) ve canlı kontrol merkezi bulanıklığını varsayılan olarak kapatır; bu yüzden kontrol merkezi düz gri görünür.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Bu uygulama root gerektirmeden bağımsız bir Şeffaf Buzlu Cam Kontrol Merkezi katmanı sunar. Üst köşeden aşağı çekerek veya Hızlı Ayarlar karesinden tek dokunuşla gerçek zamanlı şeffaflığa sahip paneli kullanabilirsiniz.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 620.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Terminal,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Gelişmiş ADB / Shizuku Komutları",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Bilgisayar (ADB) veya Shizuku üzerinden sistemin arka plan bulanıklık bayraklarını denetlemek/denemek isterseniz aşağıdaki komutları kopyalayabilirsiniz. (Not: Kilitli HyperOS sürümlerinde sistem arayüzü bunları yoksayabilir.)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    AdbCommandCard(
                        title = "1. Sistem Pencere Bulanıklığını Etkinleştir",
                        command = "adb shell settings put global disable_window_blurs 0",
                        testTag = "copy_adb_blur_global",
                        onCopy = {
                            clipboard.setText(AnnotatedString(it))
                            onCopiedCommand("ADB komutu panoya kopyalandı")
                        }
                    )

                    AdbCommandCard(
                        title = "2. HyperOS Arka Plan Bulanıklık Bayrağı",
                        command = "adb shell settings put system background_blur_enable 1",
                        testTag = "copy_adb_miui_blur",
                        onCopy = {
                            clipboard.setText(AnnotatedString(it))
                            onCopiedCommand("ADB komutu panoya kopyalandı")
                        }
                    )

                    AdbCommandCard(
                        title = "3. Cihaz Düzeyi (DeviceLevelList) Kontrolü",
                        command = "adb shell settings get system deviceLevelList",
                        testTag = "copy_adb_device_level",
                        onCopy = {
                            clipboard.setText(AnnotatedString(it))
                            onCopiedCommand("ADB komutu panoya kopyalandı")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagnosticRow(
    label: String,
    value: String,
    highlightColor: androidx.compose.ui.graphics.Color? = null
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = highlightColor ?: MaterialTheme.colorScheme.onSurface
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    }
}

@Composable
private fun AdbCommandCard(
    title: String,
    command: String,
    testTag: String,
    onCopy: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = command,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(
                onClick = { onCopy(command) },
                modifier = Modifier
                    .size(48.dp)
                    .testTag(testTag)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ContentCopy,
                    contentDescription = "Komutu kopyala",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
