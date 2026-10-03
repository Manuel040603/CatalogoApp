package com.catalogoapp.consultoras.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.catalogoapp.consultoras.ui.screens.admin.AdminConsultorasScreen
import com.catalogoapp.consultoras.ui.screens.auth.ForgotPasswordScreen
import com.catalogoapp.consultoras.ui.screens.auth.LoginScreen
import com.catalogoapp.consultoras.ui.screens.auth.RegisterScreen
import com.catalogoapp.consultoras.ui.screens.capacitacion.CapacitacionDetalleScreen
import com.catalogoapp.consultoras.ui.screens.capacitacion.CapacitacionListScreen
import com.catalogoapp.consultoras.ui.screens.capacitacion.NuevaCapacitacionScreen
import com.catalogoapp.consultoras.ui.screens.catalogo.CatalogoScreen
import com.catalogoapp.consultoras.ui.screens.devolucion.DevolucionScreen
import com.catalogoapp.consultoras.ui.screens.pedido.NuevoPedidoScreen
import com.catalogoapp.consultoras.ui.screens.pedido.PedidoListScreen
import com.catalogoapp.consultoras.ui.screens.profile.ProfileScreen
import com.catalogoapp.consultoras.ui.screens.reparto.CrearRepartoScreen
import com.catalogoapp.consultoras.ui.screens.reparto.ListaRepartosScreen
import com.catalogoapp.consultoras.ui.screens.reparto.RepartoDetalleScreen
import com.catalogoapp.consultoras.viewmodel.AdminConsultorasViewModel
import com.catalogoapp.consultoras.viewmodel.AuthViewModel
import com.catalogoapp.consultoras.viewmodel.CapacitacionDetalleViewModel
import com.catalogoapp.consultoras.viewmodel.CapacitacionViewModel
import com.catalogoapp.consultoras.viewmodel.CatalogoViewModel
import com.catalogoapp.consultoras.viewmodel.DevolucionViewModel
import com.catalogoapp.consultoras.viewmodel.NuevaCapacitacionViewModel
import com.catalogoapp.consultoras.viewmodel.PedidoViewModel
import com.catalogoapp.consultoras.viewmodel.ProfileViewModel
import com.catalogoapp.consultoras.viewmodel.RepartoViewModel

object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val RECUPERAR = "recuperar"
    const val PERFIL = "perfil"
    const val CATALOGO = "catalogo"
    const val DEVOLUCION = "devolucion"
    const val CAPACITACIONES = "capacitaciones"
    const val NUEVA_CAPACITACION = "nueva_capacitacion"
    const val CAPACITACION_DETALLE = "capacitacion_detalle"
    const val ADMIN_CONSULTORAS = "admin_consultoras"
    const val PEDIDOS = "pedidos"
    const val NUEVO_PEDIDO = "nuevo_pedido"
    const val LISTA_REPARTOS = "lista_repartos"
    const val CREAR_REPARTO = "crear_reparto"
    const val REPARTO_DETALLE = "reparto_detalle"
}

@Composable
fun CatalogoAppNavHost(authViewModel: AuthViewModel) {
    val navController: NavHostController = rememberNavController()
    val profileViewModel = remember { ProfileViewModel() }
    val pedidoViewModel = remember { PedidoViewModel() }
    val repartoViewModel = remember { RepartoViewModel() }

    val destinoInicial = if (authViewModel.usuarioActual != null) Rutas.PERFIL else Rutas.LOGIN

    NavHost(navController = navController, startDestination = destinoInicial) {

        composable(Rutas.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginExitoso = {
                    navController.navigate(Rutas.PERFIL) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                },
                onIrARegistro = { navController.navigate(Rutas.REGISTRO) },
                onIrARecuperar = { navController.navigate(Rutas.RECUPERAR) }
            )
        }

        composable(Rutas.REGISTRO) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegistroExitoso = { navController.popBackStack() },
                onIrALogin = { navController.popBackStack() }
            )
        }

        composable(Rutas.RECUPERAR) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.PERFIL) {
            val usuario = authViewModel.usuarioActual
            if (usuario != null) {
                ProfileScreen(
                    usuario = usuario,
                    viewModel = profileViewModel,
                    onCerrarSesion = {
                        authViewModel.cerrarSesion()
                        navController.navigate(Rutas.LOGIN) {
                            popUpTo(0)
                        }
                    },
                    onVerCatalogo = { navController.navigate(Rutas.CATALOGO) },
                    onIrACapacitaciones = { navController.navigate(Rutas.CAPACITACIONES) },
                    onIrAAdminConsultoras = { navController.navigate(Rutas.ADMIN_CONSULTORAS) },
                    onIrAPedidos = { navController.navigate(Rutas.PEDIDOS) },
                    onIrAReparto = { navController.navigate(Rutas.LISTA_REPARTOS) }
                )
            }
        }

        composable(Rutas.PEDIDOS) {
            PedidoListScreen(
                viewModel = pedidoViewModel,
                onVolver = { navController.popBackStack() },
                onNuevoPedido = { navController.navigate(Rutas.NUEVO_PEDIDO) }
            )
        }

        composable(Rutas.NUEVO_PEDIDO) {
            NuevoPedidoScreen(
                viewModel = pedidoViewModel,
                onVolver = { navController.popBackStack() },
                onPedidoCreado = { navController.popBackStack() }
            )
        }

        composable(Rutas.LISTA_REPARTOS) {
            ListaRepartosScreen(
                viewModel = repartoViewModel,
                onVolver = { navController.popBackStack() },
                onNuevoReparto = { navController.navigate(Rutas.CREAR_REPARTO) },
                onAbrirReparto = { idReparto -> navController.navigate("${Rutas.REPARTO_DETALLE}/$idReparto") }
            )
        }

        composable(Rutas.CREAR_REPARTO) {
            CrearRepartoScreen(
                viewModel = repartoViewModel,
                onVolver = { navController.popBackStack() },
                onRepartoCreado = { idReparto ->
                    navController.navigate("${Rutas.REPARTO_DETALLE}/$idReparto") {
                        popUpTo(Rutas.CREAR_REPARTO) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "${Rutas.REPARTO_DETALLE}/{idReparto}",
            arguments = listOf(navArgument("idReparto") { type = NavType.IntType })
        ) { backStackEntry ->
            val idReparto = backStackEntry.arguments?.getInt("idReparto") ?: 0
            RepartoDetalleScreen(
                idReparto = idReparto,
                viewModel = repartoViewModel,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.CATALOGO) {
            val catalogoViewModel = remember { CatalogoViewModel() }
            CatalogoScreen(
                viewModel = catalogoViewModel,
                onVolver = { navController.popBackStack() },
                onRegistrarDevolucion = { navController.navigate(Rutas.DEVOLUCION) }
            )
        }

        composable(Rutas.DEVOLUCION) {
            val devolucionViewModel = remember { DevolucionViewModel() }
            DevolucionScreen(
                viewModel = devolucionViewModel,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.CAPACITACIONES) {
            val capacitacionViewModel = remember { CapacitacionViewModel() }
            CapacitacionListScreen(
                viewModel = capacitacionViewModel,
                onVolver = { navController.popBackStack() },
                onAbrirCapacitacion = { id -> navController.navigate("${Rutas.CAPACITACION_DETALLE}/$id") },
                onNuevaCapacitacion = { navController.navigate(Rutas.NUEVA_CAPACITACION) }
            )
        }

        composable(Rutas.NUEVA_CAPACITACION) {
            val nuevaCapacitacionViewModel = remember { NuevaCapacitacionViewModel() }
            NuevaCapacitacionScreen(
                viewModel = nuevaCapacitacionViewModel,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Rutas.CAPACITACION_DETALLE}/{capacitacionId}",
            arguments = listOf(navArgument("capacitacionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val capacitacionId = backStackEntry.arguments?.getString("capacitacionId") ?: ""
            val uid = authViewModel.usuarioActual?.uid ?: ""
            val capacitacionDetalleViewModel = remember { CapacitacionDetalleViewModel() }
            CapacitacionDetalleScreen(
                capacitacionId = capacitacionId,
                uid = uid,
                viewModel = capacitacionDetalleViewModel,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.ADMIN_CONSULTORAS) {
            val adminConsultorasViewModel = remember { AdminConsultorasViewModel() }
            AdminConsultorasScreen(
                viewModel = adminConsultorasViewModel,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
