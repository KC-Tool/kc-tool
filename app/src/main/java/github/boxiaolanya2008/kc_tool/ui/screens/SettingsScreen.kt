package github.boxiaolanya2008.kc_tool.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import github.boxiaolanya2008.kc_tool.R
import github.boxiaolanya2008.kc_tool.manager.SettingsManager
import github.boxiaolanya2008.kc_tool.ui.components.HdrCard
import github.boxiaolanya2008.kc_tool.ui.components.HdrDivider
import github.boxiaolanya2008.kc_tool.ui.components.HdrGlowBox
import github.boxiaolanya2008.kc_tool.ui.theme.KctoolTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    modifier: Modifier = Modifier
) {
    val stealthMode by settingsManager.stealthMode.collectAsState()
    val defaultSec by settingsManager.defaultIntervalSec.collectAsState()
    val defaultMs by settingsManager.defaultIntervalMs.collectAsState()
    val autoStart by settingsManager.autoStartOnBoot.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsSection(title = stringResource(R.string.settings_general)) {
                SettingsSwitchItem(
                    icon = Icons.Default.VisibilityOff,
                    title = stringResource(R.string.settings_stealth_mode),
                    subtitle = stringResource(R.string.settings_stealth_mode_desc),
                    checked = stealthMode,
                    onCheckedChange = { settingsManager.setStealthMode(it) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                SettingsSwitchItem(
                    icon = Icons.Default.RestartAlt,
                    title = stringResource(R.string.settings_auto_start),
                    subtitle = stringResource(R.string.settings_auto_start_desc),
                    checked = autoStart,
                    onCheckedChange = { settingsManager.setAutoStartOnBoot(it) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            SettingsSection(title = stringResource(R.string.settings_defaults)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = defaultSec,
                        onValueChange = { settingsManager.setDefaultIntervalSec(it) },
                        label = { Text(stringResource(R.string.seconds)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = defaultMs,
                        onValueChange = { settingsManager.setDefaultIntervalMs(it) },
                        label = { Text(stringResource(R.string.milliseconds)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                val totalMs = remember(defaultSec, defaultMs) {
                    (defaultSec.toLongOrNull() ?: 0) * 1000 + (defaultMs.toLongOrNull() ?: 0)
                }
                Text(
                    text = stringResource(R.string.total_interval, totalMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = stringResource(R.string.settings_about),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        HdrCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    KctoolTheme {
        // Preview without real SettingsManager
    }
}