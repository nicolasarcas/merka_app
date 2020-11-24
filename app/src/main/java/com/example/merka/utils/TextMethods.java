package com.example.merka.utils;

import android.content.Context;
import android.util.Patterns;
import android.widget.Toast;

import com.example.merka.R;
import com.example.merka.activity.Registrar;
import com.example.merka.models.Loja;
import com.example.merka.models.Produto;
import com.example.merka.models.User;

import java.util.InputMismatchException;

public class TextMethods {

    public static String returnFormatedValue(String valor){

        valor = valor.replaceFirst("^0+(?!$)", "");

        if(valor.indexOf(".") == 0 || valor.indexOf(".") == valor.length() -1){
            valor = valor.replace(".", "");
        }

        int index = valor.indexOf(".");
        if (index == -1) return valor + ",00";

        if(index < valor.length()-2) valor = valor.substring(0, index+3);
        else valor += "0";

        return valor.replace('.', ',');
    }

    public static String firstLetterUpper(String text){

        if(text.length()>1){
            return text.substring(0, 1).toUpperCase() + text.substring(1);
        }
        return text;
    }

    public static String returnFormatedText(String text){

        text = text.trim();
        return firstLetterUpper(text);
    }

    public static boolean validateEqualPasswords(String password1, String password2){
        return password1.equals(password2);
    }

    public static boolean validateMinLengthPassword(String password1, String password2){
        return password1.length() > 7 || password2.length() > 7;
    }

    public static String justNumbers(String text){

        return text.replace(" ","");
    }

    public static boolean fieldsNotEmpty(String nome, String contato, String endereco, String desc, String responsavel, String cpf){
        return !nome.isEmpty() && !contato.isEmpty() && !endereco.isEmpty() && !desc.isEmpty() && !responsavel.isEmpty() && !cpf.isEmpty();
    }

    public static boolean fieldsNotEmpty(String nome, String valor, String desc){
        return !nome.isEmpty() && !valor.isEmpty() && !desc.isEmpty();
    }

    public static boolean fieldsNotEmpty(String login, String password1){
        return !login.isEmpty() && !password1.isEmpty();
    }

    public static boolean  fieldsNotEmpty(String login, String password1, String password2, String name){
        return !login.isEmpty() && !password1.isEmpty() && !password2.isEmpty() && !name.isEmpty();
    }

    public static boolean validValue(String valor){
        return valor.replace(".", "").length() > 0;
    }

    public static boolean validateMinLengthNumber(String num, int minLimit){
        return num.length() > minLimit;
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
                num = CPF.charAt(i) - 48;
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
                num = CPF.charAt(i) - 48;
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


    public static boolean validStoreFields(Context context, Loja loja){

        if(fieldsNotEmpty(loja.getNome(), loja.getContato(), loja.getEndereco(), loja.getDescricao(), loja.getResponsavel(), loja.getCpf())){

            if(cpfValido(loja.getCpf())){
                if(TextMethods.validateMinLengthNumber(loja.getContato(), 9)){

                    return true;
                }
                else Toast.makeText(context, context.getString(R.string.ToastContatoComNoMinimoDezDigitos), Toast.LENGTH_SHORT).show();
            }
            else Toast.makeText(context, context.getString(R.string.ToastDigiteCPFvalido), Toast.LENGTH_SHORT).show();
        }
        else Toast.makeText(context, context.getString(R.string.ToastPreenchaTodosCampos), Toast.LENGTH_SHORT).show();

        return false;
    }

    public static boolean validUserFields(Context context, User user, String confirmarSenha){

        if(fieldsNotEmpty(user.getNome(), user.getEmail(), user.getPassword(), confirmarSenha)) {

            if(Patterns.EMAIL_ADDRESS.matcher(user.getEmail()).matches()) {

                if(validateMinLengthPassword(user.getPassword(), confirmarSenha)) {
                    if (validateEqualPasswords(user.getPassword(), confirmarSenha)) {
                        return true;
                    }
                    else Toast.makeText(context, context.getString(R.string.ToastInsiraSenhasIguais), Toast.LENGTH_SHORT).show();
                }
                else Toast.makeText(context, context.getString(R.string.ToastSenhaComNoMinimoOitoDigitos), Toast.LENGTH_SHORT).show();
            }
            else Toast.makeText(context, context.getString(R.string.ToastEmailInvalido), Toast.LENGTH_SHORT).show();
        }
        else Toast.makeText(context, context.getString(R.string.ToastPreenchaTodosCampos), Toast.LENGTH_SHORT).show();

        return false;
    }

    public static String maskAplication(String contato) {

        return contato.substring(0,2) + " " + contato.substring(2);
    }

    public static boolean validProductFields(Context context, Produto prod){

        if(fieldsNotEmpty(prod.getNome(), prod.getValor(), prod.getDescricao())){

            if(validValue(prod.getValor())) return true;
            else Toast.makeText(context, context.getString(R.string.ToastDigiteValorValido), Toast.LENGTH_SHORT).show();
        }
        else Toast.makeText(context, context.getString(R.string.ToastPreenchaTodosCampos), Toast.LENGTH_SHORT).show();

        return false;
    }

}
