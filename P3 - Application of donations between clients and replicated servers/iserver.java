// Defines the remote interface between server and server

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface iserver extends Remote {
	// Returns the subtotal that has been to donated a certain server
	int totalDonaciones(String nombre, int id) throws RemoteException; 
	int numClientes() throws RemoteException;	// Number of clients that have donated to the N server
	public boolean registrado(String nombre) throws RemoteException;
	public void addClient(String nombre_cliente) throws RemoteException;
	public int totalDonado(String nombre)throws RemoteException;
	public void Donar(String nombre, int valor) throws RemoteException;
	public int Registrar(String nombre_cliente) throws RemoteException;
}