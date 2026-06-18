package github.boxiaolanya2008.kc_tool.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import github.boxiaolanya2008.kc_tool.R
import github.boxiaolanya2008.kc_tool.ui.theme.KctoolTheme

@Composable
fun ShizukuStatusCard(
    isConnected: Boolean,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor by animateColorAsState(
        targetValue = when {
            isConnected && hasPermission -> Color(0xFF4CAF50)
            hasPermission -> Color(0xFFFFC107)
            else -> Color(0xFFF44336)
        },
        label = "statusColor"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = stringResource(R.string.shizuku_status),
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.shizuku_status),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when {
                                isConnected -> Icons.Default.CheckCircle
                                else -> Icons.Default.Error
                            },
                            contentDescription = null,
                            tint = if (isConnected) Color(0xFF4CAF50) else Color(0xFFF44336),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isConnected) {
                                stringResource(R.string.shizuku_connected)
                            } else {
                                stringResource(R.string.shizuku_disconnected)
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when {
                                hasPermission -> Icons.Default.CheckCircle
                                else -> Icons.Default.Warning
                            },
                            contentDescription = null,
                            tint = if (hasPermission) Color(0xFF4CAF50) else Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (hasPermission) {
                                stringResource(R.string.permission_granted)
                            } else {
                                stringResource(R.string.permission_required)
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (!hasPermission) {
                    FilledTonalButton(
                        onClick = onRequestPermission
                    ) {
                        Text(text = stringResource(R.string.request_permission))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShizukuStatusCardPreview() {
    KctoolTheme {
        ShizukuStatusCard(
            isConnected = true,
            hasPermission = true,
            onRequestPermission = {}
        )
    }
}