Secuencia A:
Inicio la app. 
Ciclo_De_Vida      onCreate ejecutándose
Ciclo_De_Vida      onStart ejecutándose
Ciclo_De_Vida      onResume ejecutándose

Presionar el botón home
Ciclo_De_Vida       onPause ejecutándose
Ciclo_De_Vida      onStop ejecutándose

Volver a ejecutar la app:
Ciclo_De_Vida    onStart ejecutándose
Ciclo_De_Vida    onResume ejecutándose


Secuencia B:
Inicio la app. 
Ciclo_De_Vida      onCreate ejecutándose
Ciclo_De_Vida      onStart ejecutándose
Ciclo_De_Vida      onResume ejecutándose

Presionar el botón "Atrás/Back"
Ciclo_De_Vida     onPause ejecutándose
Ciclo_De_Vida     onStop ejecutándose


Secuencia C:
Inicio la app.
Ciclo_De_Vida   onCreate ejecutándose
Ciclo_De_Vida   onStart ejecutándose
Ciclo_De_Vida   onResume ejecutándose

Abrir temporalmente cualquier otra aplicación nativa del sistema
Ciclo_De_Vida    onPause ejecutándose
Ciclo_De_Vida    onStop ejecutándose

Vuelvo de inmediato: 
Ciclo_De_Vida      onCreate ejecutándose
Ciclo_De_Vida      onStart ejecutándose
Ciclo_De_Vida      onResume ejecutándose


Secuencia D:
Con la aplicación abierta en primer plano
Ciclo_De_Vida    onCreate ejecutándose
Ciclo_De_Vida    onStart ejecutándose
Ciclo_De_Vida    onResume ejecutándose

Rotar físicamente el teléfono poniéndolo en modo horizontal
Ciclo_De_Vida  onPause ejecutándose
Ciclo_De_Vida  onStop ejecutándose
Ciclo_De_Vida  onDestroy ejecutándose
Ciclo_De_Vida    onCreate ejecutándose
Ciclo_De_Vida    onStart ejecutándose
Ciclo_De_Vida   onResume ejecutándose

1) Al presionar el botón "Home" (Secuencia A), la aplicación pasa a segundo plano, ejecutando los métodos onPause() y onStop(), pero no se destruye la actividad, ya que permanece en memoria para una reanudación rápida.
En la Secuencia B (botón Back), se ejecutan onPause() y onStop(), y aunque no se observó onDestroy() en el Logcat, conceptualmente la actividad debería destruirse ya que la aplicación se cierra completamente.

2) Al rotar el dispositivo, Android considera este evento como un cambio de configuración. Para poder adaptar correctamente la interfaz a la nueva disposición (horizontal o vertical), el sistema decide destruir la actividad actual y recrearla completamente.
Esto implica que la ejecución de onPause(), onStop() y onDestroy(), seguido de una nueva creación con onCreate(), onStart() y onResume().
Aunque el usuario no percibe que salió de la aplicación, internamente Android reinicia la actividad para garantizar que los recursos y la interfaz se ajusten correctamente a la nueva configuración.



Ejercicio 5
Se ejecutó la aplicación varias veces, enviándola a segundo plano y volviendo a abrirla. Luego se probó rotando el dispositivo para observar el comportamiento del contador.
Logcat observado:
Estado: onResume ejecutándose - Resurrección número 1
Salir de la app y volver a entrar
Estado: onResume ejecutándose - Resurrección número 2
Salir de la app y volver a entrar
Estado: onResume ejecutándose - Resurrección número 3

Al rotar el dispositivo:
Estado: onResume ejecutándose - Resurrección número 1
Al rotar nuevamente:
Estado: onResume ejecutándose - Resurrección número 1

Observamos que el contador incrementa correctamente cada vez que la aplicación vuelve al primer plano sin ser destruida. Sin embargo, cuando ocurre un evento que destruye la actividad (como la rotación de pantalla), el contador se reinicia a 0 y vuelve a comenzar desde 1.
Esto sucede porque la variable contadorVidas se almacena en memoria dentro de la actividad, y al ejecutarse onDestroy(), la actividad es eliminada junto con sus variables, perdiendo su estado anterior.


