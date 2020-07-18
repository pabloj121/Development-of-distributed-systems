// Implements the remote interface between server and client

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Scanner;

public class client extends UnicastRemoteObject implements iclient {
	// IP del servidor al que se va a dirigir
	int ip;
	private ArrayList<server> replicas;
	iserver miserver;
	int total_donaciones;
	private Registry registro;
	String name;

	public client(int n) throws RemoteException{		
		total_donaciones = 0;
		replicas = new ArrayList<>();

		if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        // Crea el stub para el cliente especificando el nombre del servidor
        host = "server0";
        
        try {
            registro = LocateRegistry.getRegistry("localhost", 1099);
            miserver = (IcomunicacionCliente) registro.lookup(host);
        } catch (NotBoundException | RemoteException ex) {
            System.err.println("Exception del sistema: " + ex);
		}
	}
	
	// Metodo que devuelve el indice de la replica en la que hay menos usuarios registrados
	/*private int ReplicaMenos(){
		int usuarios = replicas.get(0).numClientes(); // falta
		int id_min = 0;

		for(int i = 1; i < replicas.size(); ++i){
			// Usuarios registrados en la replica i
			usuarios = replicas.get(i).numClientes();
			if(usuarios < id_min){
				id_min = i;
			}
		}

		return id_min;
	}*/

	public int registrar(String nombre_cliente) throws RemoteException{
		// Si el cliente no estaba registrado, se añade a la replica con menos entidades registradas
		
		ip = miserver.Registrar(nombre_cliente);

		return ip;
	}


	public void donar(String nombre, int valor) throws RemoteException{
		boolean encontrado = false;
		int i;

		// conectarme a la ip mia
		host = "server" + ip;    
    
        registro = LocateRegistry.getRegistry("localhost", 1099+ip);
        miserver = (IcomunicacionCliente) registro.lookup(host);
		miserver.Donar(nombre, valor);

		// Solo si el cliente está registrado puede donar
		/*for (i = 0; i < replicas.size() && !encontrado; ++i) {
			encontrado = replicas.get(i).registrado(nombre);
		}

		if(encontrado){
			replicas.get(i).donar(nombre, valor);
			total_donaciones += valor;
		}*/
	}

	public void setNombre(String nombre_cliente)throws RemoteException{
		name = nombre_cliente;
	}

	public int totalDonaciones() throws RemoteException{

		int total = miserver.totalDonaciones(name, ip);
	}


	public static void main(String[] args){
		int id_definitivo;

		//iclient mi_servidor; // servidor al que se dirigira de ahora en adelante
							 // el cliente

		String host = "localhost";
		int puerto;
		String nombre_cliente;
		Scanner teclado = new Scanner(System.in);
		int entrada = 10;
		boolean fin = false;
		int n = Integer.valueOf(args[0]); // numero de servidores
		client cliente = new client(n);

		// Crea e instala el gestor de seguridad
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		try {
			// Crea el stub para el cliente especificando el nombre del servidor
			int aleatorio = (int) Math.Random(n); // numero que va desde 0 a n-1
			String nombre = "server" + aleatorio;
			
			// En un principio contacto con un servidor aleatorio y si quiero registrarme
			// este será el que me indique a qué servidor dirigirme en adelante
			Registry registro = LocateRegistry.getRegistry(host, 1099+aleatorio);
			iclient servidor = (iclient)registro.lookup(nombre);

			entrada = 0;
			
			// Se imprime el menú de opciones hasta entrada correcta
			while(entrada < 1 || entrada > 4){
				System.out.println("Sistema de donaciones. Ingrese un número de los disponibles dependiendo de ĺa acción que desee: ");
				System.out.println("1.- REGISTRARSE EN EL SISTEMA.");
				System.out.println("2.- APORTAR DONACION AL SISTEMA.");
				System.out.println("3.- CONOCER CUANTO HA DONADO USTED AL SISTEMA.");
				System.out.println("4.- SALIR DEL SISTEMA.");
			
				entrada = Integer.parseInt(teclado.nextLine());

				if(entrada == 4){
					fin = true;
					System.out.println("Gracias por su visita. Hasta la proxima");
				}
			}

			while(!fin){
				// Dependencia de acciones

				if(entrada < 4){
					if(entrada == 1){ // registro
						System.out.println("Escriba el nombre del cliente: ");
						nombre_cliente = teclado.nextLine();
						
						id_definitivo = servidor.Registrar(nombre_cliente);

						servidor.setNombre(nombre_cliente);
					}
					else if(entrada == 2){	// donacion
						System.out.println("Escriba el nombre del cliente: ");
						nombre_cliente = teclado.nextLine();
						
						System.out.println("Ingrese la cantidad a donar: ");
						int donacion = Integer.parseInt(teclado.nextLine());

						servidor.donar(nombre_cliente, donacion);
					}
					else if(entrada == 3){	// consulta
						int donacion = servidor.totalDonado(nombre_cliente, id_definitivo);
						System.out.println("El cliente " + nombre_cliente + "ha donado " + donacion + "euros.");
					}

					System.out.println("Sistema de donaciones. Ingrese un número de los disponibles dependiendo de ĺa acción que desee: ");
					System.out.println("1.- REGISTRARSE EN EL SISTEMA.");
					System.out.println("2.- APORTAR DONACION AL SISTEMA.");
					System.out.println("3.- CONOCER CUANTO HA DONADO USTED AL SISTEMA.");
					System.out.println("4.- SALIR DEL SISTEMA.");
				
					entrada = Integer.parseInt(teclado.nextLine());					
				}
				else{
					fin = true;
				}
			}
		} 
		catch(NotBoundException | RemoteException e) {
			System.err.println("Exception del sistema: " + e);
		}
		System.exit(0);
	}
}
	