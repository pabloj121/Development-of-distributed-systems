var http = require("http");
var url = require("url");
var fs = require("fs");
var path = require("path");
var socketio = require("socket.io");

var MongoClient = require('mongodb').MongoClient;
var MongoServer = require('mongodb').Server;
var mimeTypes = { "html": "text/html", "jpeg": "image/jpeg", "jpg": "image/jpeg", "png": "image/png", "js": "text/javascript", "css": "text/css", "swf": "application/x-shockwave-flash"};

//Umbrales para el Agente
var temp_max = 25 ;
var temp_min = 15;

var lum_max = 15;
var lum_min = 8;

//Actuadores
var persiana = "bajada";
var persiana2 = 50; // al inicio está al 50%
var aire_acondicionado = "apagado";

// al inicio lo definimos como false
var auto = false;
var presencia = false;
var television = false;


var httpServer = http.createServer(
	function(request, response) {
		var uri = url.parse(request.url).pathname;
		if (uri=="/") uri = "/cliente.html";
		var fname = path.join(process.cwd(), uri);
		fs.exists(fname, function(exists) {
			if (exists) {
				fs.readFile(fname, function(err, data){
					if (!err) {
						var extension = path.extname(fname).split(".")[1];
						var mimeType = mimeTypes[extension];
						response.writeHead(200, mimeType);
						response.write(data);
						response.end();
					}
					else {
						response.writeHead(200, {"Content-Type": "text/plain"});
						response.write('Error de lectura en el fichero: '+uri);
						response.end();
					}
				});
			}
			else{
				console.log("Peticion invalida: "+uri);
				response.writeHead(200, {"Content-Type": "text/plain"});
				response.write('404 Not Found\n');
				response.end();
			}
		});
	}
);

//Conexion con la base de datos
MongoClient.connect("mongodb://localhost:27017/", { useNewUrlParser: true }, function(err, db) {
	if(!err){
		console.log("Conectado a la base de datos");
	}

	var base_datos = db.db("sisDom");

	httpServer.listen(8080);
	var io = socketio.listen(httpServer);

	// Se crea una coleccion en la base de datos
	base_datos.createCollection("sistemaDomotico", function(err, collection){
	  if(!err){
	    console.log("Colección creada en Mongo: " + collection.collectionName);
	  }
	});

	io.sockets.on('connection', function(client){
		
		// Se envia el valor de los actuadores a todos los usuarios conectados
		io.sockets.emit('estado_ac', aire_acondicionado);		
		io.sockets.emit('estado_persiana', persiana);		
		io.sockets.emit('estado_modo_automatico', auto);
		io.sockets.emit('estado_tv', television);
		io.sockets.emit('estado_presencia', presencia);

		io.sockets.emit('estado_temperatura', 18);
		io.sockets.emit('estado_luminosidad', 12);
	  	
	  	//Recogemos el valor de los sensores y lo insertamos en la DB
	  	client.on('valor_sensores', function(datos){
		    console.log('mandando valores');

		    var temperatura = datos.temperatura;
		    var luminosidad = datos.luminosidad;

		    console.log("temperatura" + temperatura);
		    console.log("luminosidad" + luminosidad);


		    base_datos.collection("SisDomCollection").insert(datos, {safe:true}, function(err, result) {
		      if(!err){
		        console.log("Valores insertados en colección de Mongo: "+ datos);
		      }
		      else{
		        console.log("Fallo en la insercion de datos.");
		      }
		    });
		    
		    io.sockets.emit('new_values',
		      "temperatura: " + datos.temperatura +
		      ", luminosidad: " + datos.luminosidad );

				//Agente. falta borrar
				/*if (datos.temperatura <= temp_min){
					console.log ("enviando mensaje temp_min");
					io.sockets.emit('alerta_temperatura', "Se ha sobrepasado la temperatura minima");
				}
				if (datos.temperatura >= temp_max){
					console.log ("enviando mensaje temp_max");
					io.sockets.emit('alerta_temperatura', "Se ha sobrepasado la temperatura maxima");
				}
				if (datos.luminosidad <= lum_min){
					console.log ("enviando mensaje lum_min");
					io.sockets.emit('alerta_luminosidad', "Se ha sobrepasado la luminosidad minima");
				}
				if (datos.luminosidad >= lum_max){
					console.log ("enviando mensaje lum_max");
					io.sockets.emit('alerta_luminosidad', "Se ha sobrepasado la luminosidad maxima");
				}*/

				//Alerta para los actuadores
				if (datos.luminosidad >= lum_max && datos.temperatura >= temp_max){
					console.log ("cerrando persiana");
					persiana = "bajada" ;
					io.sockets.emit('estado_persiana', persiana);
				}

	  	}); //Fin valor_sensores

		client.on('obtener_new_values', function(){
			base_datos.collection("SisDomCollection").find().sort({_id:-1}).limit(1).forEach(function(result){
			  client.emit('new_values',
			    "temperatura: " + result.temperatura +
			    ", luminosidad: " + result.luminosidad );

			});
		});

		///////////////// PERSIANAS //////////////////////

		// Version inicial
		client.on('cambiar_persiana', function(){
			if (persiana == "bajada") persiana="subida";
			else persiana = "bajada";

			io.sockets.emit('estado_persiana', persiana);
		});

		// Version en la cual la persiana sube un determinado porcentaje
		client.on('subir_persiana', function(){
			persiana = (persiana + (persiana*0.1)) % 100;

			io.sockets.emit('estado_persiana', persiana);
		});


		client.on('bajar_persiana', function(){
			persiana = (persiana - (persiana*0.1)) % 100;

			io.sockets.emit('estado_persiana', persiana);
		});


		client.on('cambiar_ac', function(){
			if (aire_acondicionado == "apagado") aire_acondicionado="encendido" ;
			else aire_acondicionado="apagado";
			io.sockets.emit('estado_ac', aire_acondicionado);
		});

		client.on('cambiar_automatico', function(){
			if(auto){
				auto = false;
			}
			else{
				auto = true;
			}
			io.sockets.emit('estado_modo_automatico', auto);
		});
		
		client.on('cambiar_tv', function(){
			television = !television;
			io.sockets.emit('estado_tv', television);
		});

		client.on('cambiar_presencia', function(){
			presencia = !presencia;
			io.sockets.emit('estado_presencia', presencia);
		});

	});//Fin io.sockets.on

});//Fin MongoClient