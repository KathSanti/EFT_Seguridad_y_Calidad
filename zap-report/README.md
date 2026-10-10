# Reporte de ZAP (OWASP Top 10)

En esta carpeta dejo el reporte del análisis dinámico que le hice a mi aplicación con OWASP ZAP, para cumplir con el Paso 2 de la EFT (verificar que no existan vulnerabilidades del OWASP Top 10).

## ¿Cómo lo levanté?

En vez de usar la aplicación de escritorio de ZAP, lo levanté como un contenedor de Docker, en modo "daemon" (sin ventana, corriendo en segundo plano), y lo controlé mandándole comandos a su API. Elegí hacerlo así porque es más ordenado y repetible que hacer todo a mano haciendo clic en la interfaz gráfica.

Los pasos, en simple, fueron:

1. Levanté un contenedor de ZAP aparte de mi aplicación (mi app ya estaba corriendo en Docker, en el puerto 8180).
2. Inicié sesión en mi API para conseguir un token, y le dije a ZAP que lo use automáticamente en cada petición que mande — así puede probar también las rutas que piden estar logueado, no solo el login.
3. Le fui pasando a ZAP, uno por uno, todos los endpoints de mi API (pacientes, citas, medicamentos, cuidados, facturas, usuarios) para que quedaran registrados en su mapa del sitio.
4. Con todo eso registrado, lancé el Escaneo Activo, que es el que realmente manda ataques de prueba (inyección SQL, XSS, etc.) a cada uno de esos endpoints.
5. Cuando terminó, le pedí a ZAP que generara el reporte y lo guardé acá en esta carpeta.

## Resultado

- 0 alertas de riesgo alto.
- Las alertas de riesgo medio y bajo que salieron se explican como falsos positivos o de bajo impacto real para este tipo de aplicación (el detalle de por qué está en mi informe).

## Archivos

- `reporte-zap-eft.html`: reporte completo, se puede abrir directo en el navegador.
- `reporte-zap-eft.json`: el mismo reporte en formato JSON, por si se necesita procesar los datos.
