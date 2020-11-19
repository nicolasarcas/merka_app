package com.example.merka.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.merka.R;

import java.util.InputMismatchException;

public class TextMethods {

    public static String retonarValorFormatado(String valor){

        valor = valor.replaceFirst("^0+(?!$)", "");

        if(valor.indexOf(".") == 0 || valor.indexOf(".") == valor.length() -1){
            valor = valor.replace(".", "");
        }

        int index = valor.indexOf(".");
        if (index == -1) return valor + ",00";

        Log.d("TESTE", "1" + index);
        Log.d("TESTE", "2" + valor.length());

        if(index < valor.length()-2) valor = valor.substring(0, index+3);
        else valor += "0";

        return valor.replace('.', ',');
    }

    public static String primeiraLetraMaiuscula(String text){
        if(text.length()>1){
            return text.substring(0, 1).toUpperCase() + text.substring(1);
        }
        return text;
    }

    public static String formatText(String text){

        text = text.trim();
        return primeiraLetraMaiuscula(text);
    }

    public static String justNumbers(String text){

        return text.replace(" ","");
    }

    public static boolean validateLojaFields(String nome, String contato, String endereco, String desc, String responsavel){
        return !nome.isEmpty() && !contato.isEmpty() && !endereco.isEmpty() && !desc.isEmpty() && !responsavel.isEmpty();
    }

    public static boolean validateMinLengthNumber(String num, int minLimit){
        return num.length() > minLimit;
    }

    public static boolean validateMinAndMaxLengthNumber(Context context, String num, int minLimit, int maxLimit){

        if (num.replaceAll("\\s+","").length() < minLimit){
            printToast(context.getString(R.string.ToastContatoComNoMinimoDezDigitos), context);
            return false;
        }
        else if (num.replaceAll("\\s+","").length() > maxLimit){
            printToast(context.getString(R.string.ToastMaximoDeDigitos), context);
            return false;
        }
        return true;
    }


    private static void printToast(String value, Context context){
        Toast.makeText(context, value, Toast.LENGTH_SHORT).show();
    }

    public static boolean cpfValido(String CPF) {
        // considera-se erro CPF's formados por uma sequencia de numeros iguais
        if (CPF.equals("00000000000") ||
                CPF.equals("11111111111") ||
                CPF.equals("22222222222") || CPF.equals("33333333333") ||
                CPF.equals("44444444444") || CPF.equals("55555555555") ||
                CPF.equals("66666666666") || CPF.equals("77777777777") ||
                CPF.equals("88888888888") || CPF.equals("99999999999") ||
                (CPF.length() != 11))
            return(false);

        char dig10, dig11;
        int sm, i, r, num, peso;

        // "try" - protege o codigo para eventuais erros de conversao de tipo (int)
        try {
            // Calculo do 1o. Digito Verificador
            sm = 0;
            peso = 10;
            for (i=0; i<9; i++) {
                // converte o i-esimo caractere do CPF em um numero:
                // por exemplo, transforma o caractere '0' no inteiro 0
                // (48 eh a posicao de '0' na tabela ASCII)
                num = (int)(CPF.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso - 1;
            }

            r = 11 - (sm % 11);
            if ((r == 10) || (r == 11))
                dig10 = '0';
            else dig10 = (char)(r + 48); // converte no respectivo caractere numerico

            // Calculo do 2o. Digito Verificador
            sm = 0;
            peso = 11;
            for(i=0; i<10; i++) {
                num = (int)(CPF.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso - 1;
            }

            r = 11 - (sm % 11);
            if ((r == 10) || (r == 11))
                dig11 = '0';
            else dig11 = (char)(r + 48);

            // Verifica se os digitos calculados conferem com os digitos informados.
            return (dig10 == CPF.charAt(9)) && (dig11 == CPF.charAt(10));
        } catch (InputMismatchException erro) {
            return(false);
        }
    }
}
