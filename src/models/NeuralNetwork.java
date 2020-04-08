package models;

import java.util.List;

public class NeuralNetwork {

    private Matrix weights_ih; //pesos entre a input layer e hidden layer
    private Matrix weights_ho; //pesos entre a hidden layer e output layer
    private Matrix bias_h; //bias dos hidden nodes
    private Matrix bias_o; //bias dos output node
    private double learning_rate; //taxa de aprendizado

    public NeuralNetwork() {}

    public NeuralNetwork(int size_input, int size_target, int size_hidden) {
        //cria as matrizes de pesos
        weights_ih = new Matrix(size_hidden, size_input);
        weights_ho = new Matrix(size_target, size_hidden);

        //cria os bias
        bias_h = new Matrix(size_hidden, 1);
        bias_o = new Matrix(size_target, 1);

        //inicializa a taxa de aprendizado
        learning_rate = 0.1;
    }

    //funcao de ativacao sigmoide f(x) = 1 / (1 + exp(-x))
    public static void activationFunction(Matrix m) {
        /*
        o "x" da funcao de ativacao sera o valor de cada linha da matriz
         */
        for(int i = 0; i < m.getRows(); i++) {
            for(int j = 0; j < m.getCols(); j++) {
                double x = m.getValueMatrix(i, j);
                double result = 1 / (1 + Math.exp(-x));
                m.setValueMatrix((result), i, j);
            }
        }

    }

    //funcao de gradiente, derivada da funcao sigmoide f'(x) = f(x) * (1 - f(x))
    public static Matrix gradientFunction(Matrix m) {

        Matrix res = new Matrix(m.getRows(), m.getCols());

        for(int i = 0; i < m.getRows(); i++) {
            for(int j = 0; j < m.getCols(); j++) {
                double result = m.getValueMatrix(i, j) * (1 - m.getValueMatrix(i, j));
                res.setValueMatrix(result, i, j);
            }
        }

        return res;
    }

    /*
    Metodo que serve para os testes depois de treinar a rede neural
     */
    public List<Double> test(List<Double> input) {
        /*
        transforma a lista de inputs em uma matriz de inputs.
        sempre sera uma matriz com uma unica coluna
         */
        Matrix inputs = Matrix.fromArray(input);

        /*
        - multiplicacao dos pesos e inputs que ficarao armazenados na matriz hidden
        - adiciona o bias ao resultado da multiplicacao entre os pesos e inputs
        - gera o output dos hidden nodes atraves da funcao de ativacao
         */
        Matrix hidden = Matrix.multiplyMatrix(this.weights_ih, inputs);
        Matrix.addMatrix(hidden, this.bias_h);
        NeuralNetwork.activationFunction(hidden);

        /*
        - multiplicacao dos pesos e hidden nodes que ficarao armazenados na matriz output
        - adiciona o bias ao resultado da multiplicacao entre os pesos e o output gerado pelos hidden nodes
        - gera o output dos output nodes atraves da funcao de ativacao
         */
        Matrix output = Matrix.multiplyMatrix(this.weights_ho, hidden);
        Matrix.addMatrix(output, this.bias_o);
        NeuralNetwork.activationFunction(output);

        return output.toArray();
    }

    /*
    Metodo que faz o Feedforward, e, logo apos, o Brackpropagation
     */
    public void train(List<Double> input, List<Double> target) {
        /*********        FEEDFORWARD         ************/

        /*
        transforma a lista de inputs em uma matriz de inputs.
        sempre sera uma matriz com uma unica coluna
         */
        Matrix inputs = Matrix.fromArray(input);

        /*
        - multiplicacao dos pesos e inputs que ficarao armazenados na matriz hidden
        - adiciona o bias ao resultado da multiplicacao entre os pesos e inputs
        - gera o output dos hidden nodes atraves da funcao de ativacao
         */
        Matrix hidden = Matrix.multiplyMatrix(this.weights_ih, inputs);
        Matrix.addMatrix(hidden, this.bias_h);
        NeuralNetwork.activationFunction(hidden);

        /*
        - multiplicacao dos pesos e hidden nodes que ficarao armazenados na matriz output
        - adiciona o bias ao resultado da multiplicacao entre os pesos e o output gerado pelos hidden nodes
        - gera o output dos output nodes atraves da funcao de ativacao
         */
        Matrix output = Matrix.multiplyMatrix(this.weights_ho, hidden);
        Matrix.addMatrix(output, this.bias_o);
        NeuralNetwork.activationFunction(output);

        /*********        BACKPROPAGATION         ************/

        /*
        transforma a lista de target em uma matriz de targets.
        sempre sera uma matriz com uma unica coluna
         */
        Matrix targets = Matrix.fromArray(target);

        /*        Calculo do Erro do Output gerado pelo feedforward         */
        // ERRO = Target - Output
        Matrix output_errors = Matrix.subtractMatrix(targets, output);

        /*        Calculo do gradiente do output         */
        //output_gradient_aux = learning_rate * Erro * f'(x)
        Matrix output_gradient = NeuralNetwork.gradientFunction(output);
        Matrix output_gradient_aux = Matrix.multiplyMatrix(output_gradient, output_errors);
        Matrix.multiplyScalar(output_gradient_aux, this.learning_rate);

        /*        Calculo dos deltas do hidden->output layer         */
        // Delta(W_ho) = output_gradient_aux * Hidden(t)
        Matrix hidden_trans = Matrix.transpose(hidden);
        Matrix weights_ho_deltas = Matrix.multiplyMatrix(output_gradient_aux, hidden_trans);

        /*        Ajuste dos pesos da matriz do hidden->output layer         */
        Matrix.addMatrix(this.weights_ho, weights_ho_deltas);

        /*        Ajuste dos bias do output layer         */
        Matrix.addMatrix(this.bias_o, output_gradient_aux);

        /*        Calculo do erro dos hidden nodes         */
        //matriz transposta dos pesos entre a hidden layer e a output layer
        Matrix weight_ho_trans = Matrix.transpose(this.weights_ho);
        Matrix hidden_errors = Matrix.multiplyMatrix(weight_ho_trans, output_errors);

        /*        Calculo do gradiente do hidden layer         */
        //hidden_gradient_aux = learning_rate * Erro * f'(x)
        Matrix hidden_gradient = NeuralNetwork.gradientFunction(hidden);
        Matrix hidden_gradient_aux = Matrix.multiplyMatrix(hidden_gradient, hidden_errors);
        Matrix.multiplyScalar(hidden_gradient_aux, this.learning_rate);

        /*        Calculo dos deltas do input->hidden layer         */
        // Delta(W_ih) = hidden_gradient_aux * Input(t)
        Matrix input_trans = Matrix.transpose(inputs);
        Matrix weights_ih_deltas = Matrix.multiplyMatrix(hidden_gradient_aux, input_trans);

        /*        Ajuste dos pesos da matriz input->hidden layer         */
        Matrix.addMatrix(this.weights_ih, weights_ih_deltas);

        /*        Ajuste dos bias do hidden layer         */
        Matrix.addMatrix(this.bias_h, hidden_gradient_aux);

    }

}
