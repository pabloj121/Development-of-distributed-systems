#include "dns.h"

int main (int argc, char *argv[])
{
	
	char *host;
	CLIENT *clnt;
	double  *result_1;
	int consultar;
	int *result_2;
	double no_consultar;
	int numero;

	host = argv[1];
	clnt = clnt_create(argv[1], DNSPROG, DNSVERS, "udp");
	numero = atoi(argv[2]);
	consultar = atoi(argv[3]);
	
	if (numero == 0){
		result_1 = consultar_1(consultar, clnt);
		if(*result_1 != -1)
			printf("IP equipo: %f\n",*result_1);
		else
			printf("No se ha encontrado el equipo\n");
	}
	else if (numero == 1){
		no_consultar = atoi(argv[4]);
		result_2 = registrar_1(consultar, no_consultar, clnt);
		if(*result_2)
			printf("Cliente registrado\n");
	}
	else if (numero == 2){
		result_2 = baja_1(consultar, clnt);
		if(*result_2)
			printf("Cliente dado de baja\n");
		else
			printf("El equipo no se ha encontrado\n");
	}

clnt_destroy (clnt);

exit (0);
}
