package com.myhub.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myhub.app.R
import com.myhub.app.ui.navigation.Route

@Composable
fun AppDrawerContent(
    currentRoute: String,
    onItemClick: (Route) -> Unit
) {
    ModalDrawerSheet {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "MyHub Logo",
                modifier = Modifier.size(72.dp)
            )
        }
        HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))

        NavigationDrawerItem(
            label = { Text(stringResource(R.string.drawer_access)) },
            icon = { Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = "Access") },
            selected = currentRoute == Route.Access.path,
            onClick = { onItemClick(Route.Access) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.preferenze)) },
            icon = { Icon(Icons.AutoMirrored.Filled.ViewList, contentDescription = "Preferenze") },
            selected = currentRoute == Route.Preferenze.path,
            onClick = { onItemClick(Route.Preferenze) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.impostazioni)) },
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Impostazioni") },
            selected = currentRoute == Route.Impostazioni.path,
            onClick = { onItemClick(Route.Impostazioni) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}
