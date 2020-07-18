/* Archivo dns.x: Protocolo de DNS */

program DNSPROG {
	version DNSVERS {
		double CONSULTAR (int) = 1;
		int REGISTRAR (int, double) = 2;
		int BAJA (int) = 3;
		int ENTRADAS (int) = 4;
	} = 1;
} = 0x20000001;
