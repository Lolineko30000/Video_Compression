function Enviar(elNombre){
    fetch( 'http://localhost:8080/?mensaje=' + elNombre ) // URL reconocida por la aplicación java principal
    .then(response => {
        if (!response.ok) {
			throw new Error('La solicitud no pudo ser completada correctamente.');
        }
        return response.text();  // debe ser "text" en minúsculas
        //return response.json(); // Si se espera una respuesta JSON
    })
    .then(data => {
        resultado.innerHTML = "Hola " + elNombre + ", el servidor respondio: <BR> <U>" + data+"</U>"; 
        //console.log(data); // Muestra los datos obtenidos en la "consola" del navegador, visible usando [F12]
    })
    .catch(error => {
		alert('Hubo un error al realizar la solicitud:');  // Muestra error en pantalla
        // console.error('Hubo un error al realizar la solicitud:', error);  // Manda error a la consola del navegador
    });

}