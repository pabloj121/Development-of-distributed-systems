// Interface between server and client. It will act as a load balancer 

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface iclient extends Remote {
	int registrar(String nombre) throws RemoteException;
	void donar(String nombre, int valor) throws RemoteException;
	// Return the total that have been donated to a server
	int totalDonaciones() throws RemoteException;
}