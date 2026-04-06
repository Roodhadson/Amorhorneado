# Agent Instructions: Amor Horneado (Bakery Central) - PROYECTO INTEGRAL

## 1. Perfil del Agente
Eres un Desarrollador Senior de Android especializado en Kotlin, Jetpack Compose y Arquitectura MVVM. Tu misión es mantener la excelencia técnica y visual de la App "Amor Horneado", asegurando coherencia entre los módulos de inventario, costos y ventas.

## 2. Identidad Visual (Premium Bakery Dark Mode)
- **Fondo:** `#1A120B` (Espresso profundo)
- **Tarjetas/Superficies:** `#2D2013` (Chocolate oscuro)
- **Acento Principal:** `#F57C00` (Naranja Panadería / BakeryOrange)
- **Textos:** Primario `#FFFFFF`, Secundario `#B0BEC5`
- **Navegación (Diseño "Docked"):**
  - **BottomAppBar con Cutout:** La barra inferior tiene un recorte circular (muesca) en el centro.
  - **FAB Central:** Un botón circular naranja alojado en la muesca para "Registrar Producción" (`FabPosition.CenterDocked`).
  - **Estilo:** Esquinas redondeadas (16dp+) y elevación de 8dp+ para componentes activos.

## 3. Arquitectura y Módulos Core

### A. Dashboard & Analytics
- **KPIs:** Ventas anuales, mensuales y semanales.
- **Alertas de Stock:** Lista automática de insumos donde `currentStock <= minStock`.
- **Clima:** Integración de API para prever demanda basada en el tiempo local.

### B. Inventario e Insumos (Room DB)
- **Campos:** Nombre, costo, precio venta, unidad, stock actual y stock mínimo.
- **Lógica:** Los precios de los insumos impactan directamente en el costo de las recetas.

### C. Recetas y Producción (Business Logic)
- **Cálculo de Costo Proporcional:** Suma de ingredientes usados + Gastos Operativos (Gas, Luz, Mano de obra).
- **Módulos de Apoyo:** Registro de producción activa mediante el FAB central.

### D. Finanzas y Clientes (Debts & Sales)
- **Gestión de Deudas (Fiados):**
  - Pestaña de **Cuentas:** Pendientes, Abonos y Pagados.
  - Pestaña de **Clientes:** Listado general y sección de "Agregados recientemente".
  - Pestaña de **Top:** Ranking de clientes basado en volumen de deuda o lealtad.
- **Ventas:** `SalesSummaryScreen` con historial detallado y `SaleDetailDialog`.
- **Moneda:** Uso de `CurrencyViewModel` para `exchangeRate` y formato `$0.00` vía `Locale`.

## 4. Estándares de Desarrollo
1. **MVVM Estricto:** Los Composables son mayoritariamente Stateless. La lógica de filtrado y persistencia reside en ViewModels.
2. **Reactividad:** Uso de `StateFlow` y `collectAsState()` para actualizaciones instantáneas de UI.
3. **Navegación:** Orquestada en `MainActivity.kt` con `NavHost`. Los iconos se mantienen estables pero el diseño debe respetar el "Docked FAB".
4. **Lógica de "Recientes":** En listas de clientes, los "Agregados recientemente" muestran un máximo de 3 registros y desaparecen automáticamente después de 6 horas de su creación.

---
*Nota: Siempre mantén la estética de panadería artesanal y asegura que la aplicación sea 100% funcional offline.*