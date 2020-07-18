// Implements the remote interface between server/server

import java.rmi.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.lang.Thread;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Pair;


public class server extends UnicastRemoteObject implements iserver {
	private int sub_total;	// total de las donaciones realizadas en esta replica (subtotal?)
	int idservidor;
	int next_id;
	int n_servers;
	// En cada campo se guardarán nombre del cliente y lo que ha donado en total 
	private HashMap<String, Integer> donaciones;
	private iserver servidor; // se usara para saber a que servidor dirigirse

	public server(int id, int num) throws RemoteException{
		donaciones = new HashMap<String, Integer>();
		sub_total = 0;
		idservidor = id; // indicativo del id
		next_id = (idservidor+1) % n_servers;
		n_servers = num; // numero de servidores funcionando al mismo tiempo
	}
	
	private void connectNextServer(){
		if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
        	next_id = (idservidor+1) % n_servers;
            String nombre = "server" + next_id;

            // Crea el stub para el cliente especificando el nombre del servidor
            Registry registro = LocateRegistry.getRegistry("localhost", 1099+next_id);
            servidor = (iserver) registro.lookup(nombre);

        } catch (NotBoundException | RemoteException e) {
            System.err.println("Excepcion al intentar conectar con el siguiente servidor: " + e);
        }
	}

	// Connect to a certain server
	private void connectServer(int id){
		if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            String nombre = "server" + id;

            // Crea el stub para el cliente especificando el nombre del servidor
            Registry registro = LocateRegistry.getRegistry("localhost", 1099+id);
            servidor = (iserver) registro.lookup(nombre);

        } catch (NotBoundException | RemoteException e) {
            System.err.println("Excepcion al conectar con el servidor" + id + ": " + e);
        }
	}


	public int numClientes() throws RemoteException{
		return donaciones.size();
	}


	// Returns the ID's server less used
	public int MenosUsado() throws RemoteException{
		// Number of clients on this server
		int n_clients = donaciones.size();
		int id_bueno = idservidor;

		while(next_id != idservidor){
			connectNextServer();	
			if(servidor.numClientes() < n_clients){
				n_clients = servidor.numClientes();	
				id_bueno = next_id;
			}
		}

		return id_bueno;
	}


	// Si el cliente no estaba registrado, se añade a nuestra replica
	public int Registrar(String nombre_cliente) throws RemoteException{
		String nombre = nombre_cliente;
		boolean registrado = false;
		int server_registry = -1;

        // si no esta en mi servidor, entonces miro a ver si está en los demas
        if(!donaciones.containsKey(nombre_cliente)){
			while(next_id != idservidor && !registrado){
				connectNextServer();	
				if(servidor.registrado(nombre_cliente)){
					registrado = true;
				}
			}

			// se han de reestablecer los valores iniciales para los posteriores recorridos de las replicas
			next_id = (idservidor+1) % n_servers;

			if(!registrado){
				server_registry = MenosUsado();	// servidor con menos clientes
				connectServer(server_registry);		// nos conectamos al servidor con menos clientes
				servidor.addClient(nombre_cliente);	// se registra al cliente en el servidor menos usado
			}
			
		}
		else{
			System.out.println("Este nombre ya está en uso. Vuelva a repetir el proceso.");
		}

		return server_registry;
	}


	public void addClient(String nombre_cliente)throws RemoteException{
		//donaciones.putIfAbsent(nombre_cliente, 0);
		donaciones.put(nombre_cliente, 0);
	}

	

	// hay que conectarse la servidor en el que el cliente se registró. falta.
	public void Donar(String nombre_cliente, int valor) throws RemoteException{
		// Saldo donado hasta el momento por el cliente 'nombre'
		if(donaciones.containsKey(nombre_cliente)){
			int money = donaciones.get(nombre_cliente);
			donaciones.put(nombre_cliente, money+valor);

			sub_total += valor;

		}

	}

	// Devuelve el total que se le ha donado a un determinado servidor
	public int totalDonaciones(String nombre, int id) throws RemoteException {
		connectServer(id);

		// si el cliente ha realizado alguna donacion
		if(servidor.totalDonado(nombre) > 0){
			return sub_total;
		}

		return -1;
	}

	// Metodo que devuelve el total donado por el cliente
	public int totalDonado(String nombre) throws RemoteException{		
		return donaciones.get(nombre);
	}

	public boolean registrado(String nombre) throws RemoteException{
		if(donaciones.containsKey(nombre)){
			return true;
		}
		return false;
	}

	public static void main(String[] args) {
		//int n = (int) args[1]; // numero de servidores a lanzar
		int n = Integer.valueOf(args[0]);
		//ArrayList<int> servidores = new ArrayList<int>(n);
		int id_server;

		// Crea e instala el gestor de seguridad
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		try {
			// Crea una instancia de contador
			//System.setProperty("java.rmi.server.hostname","192.168.1.107");
			
			// direcciones 1099 y 1100
			for (id_server = 0; id_server < n ; ++id_server) {
				Registry reg = LocateRegistry.createRegistry(1099 + id_server);
				server miserver = new server(id_server, n);
				String nombre = "server" + id_server;
				Naming.rebind(nombre, miserver);
				System.out.println("Servidor RemoteException | MalformedURLExceptiondor preparado");
			}			
		} 
		catch (RemoteException | MalformedURLException e) {
			System.out.println("Exception: " + e.getMessage());
		}

	}
}