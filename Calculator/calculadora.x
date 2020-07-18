/* Archivo msg.x: Protocolo de impresion de una calculadora */

program CALCULADORAPROG {
	version CALCULADORAVERS {
		int SUMAR (int, int) = 1;
		int RESTAR (int, int) = 2;
		int MULTIPLICAR (int, int) = 3;
		int DIVIDIR (int, int) = 4;
	} = 1;
} = 0x20000001;
