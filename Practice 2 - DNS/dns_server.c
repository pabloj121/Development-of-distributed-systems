#include "dns.h"

typedef struct{
	int nombre;
	double ip;
}pareja;

pareja *pablo;
int TAM;

void inicializar(int tipo){
int nombre; double ip;int i;

	if(tipo == 1){
		TAM = 11; nombre=11; ip=1.1;
		pablo = malloc(sizeof(pareja)*TAM);
		for(i=0; i<TAM-2; i++){
			pablo[i].nombre = nombre; pablo[i].ip = ip;
			nombre+=1; ip+=0.1;
		}

		pablo[TAM-2].nombre = 20; pablo[TAM-2].ip = 2.0;
		pablo[TAM-1].nombre = 30; pablo[TAM-1].ip = 3.0;
	}

	if(tipo == 2){
		TAM = 10; nombre=21; ip=2.1;
		pablo = malloc(sizeof(pareja)*TAM);
		for(i=0; i<TAM-1; i++){
			pablo[i].nombre = nombre; pablo[i].ip = ip;
			nombre+=1; ip+=0.1;
		}

		pablo[TAM-1].nombre = 10; pablo[TAM-1].ip = 1.0;
	}

	if(tipo == 3){
		TAM = 10; nombre=31; ip=3.1;
		pablo = malloc(sizeof(pareja)*TAM);
		for(i=0; i<TAM-1; i++){
			pablo[i].nombre = nombre; pablo[i].ip = ip;
			nombre+=1; ip+=0.1;
		}

		pablo[TAM-1].nombre = 10; pablo[TAM-1].ip = 1.0;
	}


}

double *
consultar_1_svc(int arg1,  struct svc_req *rqstp){
	static double  result;
	int i;
	char * localhost = "localhost";
	CLIENT *clnt;

	for(i=0; i<TAM; i++){
		if (pablo[i].nombre == arg1){
			result = pablo[i].ip;
			break;
		}
	}
	if(i == TAM){
		if((arg1 > 20) && (arg1 < 30)){
			clnt = clnt_create(localhost, DNS2, DNS2VERS,"udp");
			result = *consultar_1(arg1, clnt);
			clnt_destroy (clnt);
		}
		else if((arg1 > 30) && (arg1 < 40)){
			clnt = clnt_create(localhost, DNS3, DNS3VERS,"udp");
			result = *consultar_1(arg1, clnt);
			clnt_destroy (clnt);
		}
		else
			result = -1;

	}

	return &result;
}

int *
registrar_1_svc(int arg1, double arg2,  struct svc_req *rqstp){
	static int  result =1;
	pareja *aux;
	aux = malloc(sizeof(pareja)*TAM);

	int i;
	for(i = 0; i<TAM; i++){
		aux[i] = pablo[i];
	}

	TAM++;
	pablo = malloc(sizeof(pareja)*TAM);

	for(i = 0; i<TAM-1; i++){
		pablo[i] = aux[i];
	}

	pablo[i].nombre = arg1;
	pablo[i].ip = arg2;

	for(i=0; i<TAM; i++)
		printf("Nombre equipo: %i - Ip equipo: %f\n", pablo[i].nombre, pablo[i].ip);

	return &result;
}

int *
baja_1_svc(int arg1,  struct svc_req *rqstp)
{
	static int  result = 1;
	int i;
	pareja *aux;

	for(i=0; i<TAM; i++){
		if(pablo[i].nombre == arg1){
			break;
		}
	}
	if(i<TAM){
		for(; i<TAM-1; i++){
			pablo[i] = pablo[i+1];
		}
		TAM--;
		aux = malloc(sizeof(pareja)*TAM);

		for(i=0; i<TAM; i++){
			aux[i] = pablo[i];
		}
		pablo = malloc(sizeof(pareja)*TAM);

		for(i=0; i<TAM; i++){
			pablo[i] = aux[i];
		}
	}
	else
		result = 0;
		
	return &result;
}
