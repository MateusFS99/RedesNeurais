package trabredesneurais;

import Models.Neuronio;
import Models.Treino;
import util.MaskFieldUtil;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;

public class TreinamentoController implements Initializable {

    private CSVReader csvr;
    private List<Treino> t;
    private double[] maior, menor;
    private int[][] matrix;
    private double[][] ocultapeso, saidapeso;
    private List<double[][]> focutapeso, fsaidapeso;
    private List<String> classes;
    
    @FXML
    private ToggleGroup gay;
    @FXML
    private JFXTextField txentrada;
    @FXML
    private JFXTextField txsaida;
    @FXML
    private JFXTextField txoculta;
    @FXML
    private JFXTextField txiteracoes;
    @FXML
    private JFXTextField txerro;
    @FXML
    private JFXRadioButton rblin;
    @FXML
    private JFXRadioButton rblog;
    @FXML
    private JFXTextField txaprendizagem;
    @FXML
    private JFXRadioButton rbhiper;
    @FXML
    private TableView<List<String>> tvcsv;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        MaskFieldUtil.numericField(txentrada);
        MaskFieldUtil.numericField(txoculta);
        MaskFieldUtil.numericField(txsaida);
        MaskFieldUtil.numericField(txiteracoes);
    }    

    @FXML
    private void clkArq(ActionEvent event) throws FileNotFoundException, IOException, CsvValidationException 
    {
        FileChooser fc = new FileChooser();
        
        fc.setTitle("Abrir Arquivo CSV");
        Reader reader = Files.newBufferedReader(Paths.get(fc.showOpenDialog(null).toURI()));
        csvr = new CSVReader(reader);

        List<String> colstrings = Arrays.asList(csvr.readNext());
        for (int j = 0; j < colstrings.size(); j++) 
        {
            final int i = j;
            TableColumn<List<String>,String> col = new TableColumn<>(colstrings.get(j).toUpperCase());
            col.setCellValueFactory((v) -> new SimpleStringProperty(v.getValue().get(i)));
            tvcsv.getColumns().add(col);
        }
        
        int entrada = colstrings.size()-1;
        txentrada.setText(""+entrada);
        
        classes = new ArrayList<>();
        String[] line;
        List<String> lc;
        maior = new double[colstrings.size()-2];
        menor = new double[colstrings.size()-2];
        
        for (int i = 0; i < maior.length; i++) 
        {
            maior[i] = Double.MIN_VALUE;
            menor[i] = Double.MAX_VALUE;
        }
        
        List<List<String>> l = new ArrayList<>();
        
        while((line = csvr.readNext()) != null)
        {
            lc = Arrays.asList(line);
            for (int i = 0; i < maior.length; i++)
            {
                if(Integer.parseInt(line[i]) > maior[i])
                    maior[i] = Double.parseDouble(line[i]);
                if(Integer.parseInt(line[i]) < menor[i])
                    menor[i] = Double.parseDouble(line[i]);
            }
            
            if(!classes.contains(lc.get(lc.size()-1)))
                classes.add(lc.get(lc.size()-1));
            tvcsv.getItems().add(lc);
            l.add(lc);
        }  
        
        geraCamadaEntrada(l);
        
        int saida = classes.size();
        txsaida.setText(""+saida);
        txoculta.setText(""+((entrada+saida)/2));
    }
    
    private void geraCamadaEntrada(List<List<String>> l)
    {
        Treino tr;
        double[] diferenca = new double[maior.length];
        t = new ArrayList<>();
        for (int i = 0; i < maior.length; i++) 
            diferenca[i] = maior[i]-menor[i];
        for (int j = 0; j < l.size(); j++) 
        {
            tr = new Treino();
            for (int k = 0; k < maior.length; k++) 
                tr.getEntradas().add((Double.parseDouble(l.get(j).get(k))-menor[k])/diferenca[k]);
            t.add(tr);
        }
    }

    @FXML
    private void clkTreinar(ActionEvent event) 
    {
        boolean flag = true;
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        
        a.setTitle("Treinar");
        a.setHeaderText("Atenção!");
        
        if(txoculta.getText().equals("")) {
            
            a.setContentText("Quantidade de Camadas Ocultas é Necessaria");
            a.showAndWait();
            flag = false;
        }
        if(txentrada.getText().equals("")) {
            
            a.setContentText("Quantidade de Camadas de Entrada é Necessaria");
            a.showAndWait();
            flag = false;
        }
        if(txsaida.getText().equals("")) {
            
            a.setContentText("Quantidade de Camadas de Saída é Necessaria");
            a.showAndWait();
            flag = false;
        }
        if(txaprendizagem.getText().equals("")) {
            
            a.setContentText("Taxa de Aprendizagem é Necessaria");
            a.showAndWait();
            flag = false;
        }
        if(csvr == null) {
            
            a.setContentText("Arquivo Não Aberto");
            a.showAndWait();
            try { clkArq(null); } catch(Exception e) {}
            flag = false;
        }
        if(flag) {
        
            double aprendizagem = Double.parseDouble(txaprendizagem.getText());
            int oculta = Integer.parseInt(txoculta.getText());
            int entrada = Integer.parseInt(txentrada.getText());
            int saida = Integer.parseInt(txsaida.getText());
            int aux = Integer.parseInt(txaprendizagem.getText());

            flag = true;
            if(aux < 0 || aux > 1)
            {
                //ATENCAO ATENCAO, O CARRO DO OVO ESTÁ PASSANDO NA SUA RUA
                //OLHA O CARRO DO DANONE EINNNNNN
                a.setContentText("Taxa Inválida! Insira um Valor Entre 0 e 1");
                a.showAndWait();
                flag = false;
            }
            if(flag)
            {
                geraMatrizDesejada();
                double erroatual = Double.MAX_VALUE;
                double[][] moculta = new double[oculta][entrada];
                double[][] msaida = new double[saida][oculta];

                for (Treino treino : t) 
                {
                    treino.getOculta().setCamada(entrada, oculta, saida);
                    for (int i = 0; i < saida; i++) 
                        treino.getSaidas().add(new Neuronio());
                    treino.getOculta().rPeso(entrada, saida);
                }

                for (int i = 0; i < saida; i++)
                {
                    focutapeso.add(moculta);
                    fsaidapeso.add(msaida);
                }

                int it = Integer.parseInt(txiteracoes.getText());
                double ermin = Double.parseDouble(txerro.getText());
                int saidad = 0;
                
                for (int i = 0; i < it && erroatual > ermin; i++) 
                {
                    erroatual = 0;
                    for (int k = 0; k < t.size(); k++) 
                    {
                        for (int j = 0; j < classes.size(); j++) 
                            if(classes.get(j).equals(tvcsv.getItems().get(k).get(entrada)))
                            {
                                j = classes.size();
                                saidad = j;
                            }

                        focutapeso.set(saidad,t.get(k).getOculta().getOcultapeso());
                        fsaidapeso.set(saidad,t.get(k).getOculta().getSaidapeso());

                        for (int j = 0; j < oculta; j++) 
                        {
                            t.get(k).getOculta().getNeuronio().get(j).calculaNet(j,t.get(k).getEntradas(),t.get(k).getOculta().getOcultapeso());
                            if(rblin.isSelected())
                                t.get(k).getOculta().getNeuronio().get(j).setLinear();
                            else if(rblog.isSelected())
                                t.get(k).getOculta().getNeuronio().get(j).SetLogistica();
                            else
                                t.get(k).getOculta().getNeuronio().get(j).setHiperbolica();
                        }
                        
                        List<Double> oc = new ArrayList<>();
                        for (int j = 0; j < oculta; j++) 
                            oc.add(t.get(k).getOculta().getNeuronio().get(j).getNetr());
                        
                        for (int j = 0; j < saida; j++) 
                        {
                            t.get(k).getSaidas().get(j).calculaNet(j, oc, t.get(k).getOculta().getSaidapeso());
                            
                            if(rblin.isSelected())
                                t.get(k).getSaidas().get(j).setLinear();
                            else if(rblog.isSelected())
                                t.get(k).getSaidas().get(j).SetLogistica();
                            else
                                t.get(k).getSaidas().get(j).setHiperbolica();
                            
                            t.get(k).getSaidas().get(j).calculaErroS(matrix[j][saidad]);
                        }
                        
                        List<Double> erroS = new ArrayList<>();
                        for (int j = 0; j < saida; j++) 
                            erroS.add(t.get(k).getSaidas().get(j).getErro());
                        
                        for (int j = 0; j < oculta; j++)
                            t.get(k).getOculta().getNeuronio().get(j).calculaErroOculta(j, erroS, t.get(k).getOculta().getOcultapeso());
                        
                        t.get(k).getOculta().corrigePesoS(aprendizagem, erroS);
                        t.get(k).getOculta().corrigePesoO(aprendizagem, erroS);
                        
                        t.get(k).calcularER();
                        
                        erroatual += t.get(k).getErro();
                        ocultapeso = t.get(k).getOculta().getOcultapeso();
                        saidapeso = t.get(k).getOculta().getSaidapeso();
                        moculta = ocultapeso;
                        msaida = saidapeso;
                    }
                    erroatual /= t.size();
                }
            }
        }
    }
    
    private void geraMatrizDesejada()
    {
        int saida = Integer.parseInt(txsaida.getText());
        matrix = new int[saida][saida];
        
        for (int i = 0; i < saida; i++) 
            for (int j = 0; j < saida; j++) 
                if(rbhiper.isSelected())
                    if(i == j)
                        matrix[i][j] = 1;
                    else
                        matrix[i][j] = -1;
                else
                    if(i == j)
                        matrix[i][j] = 1;
                    else
                        matrix[i][j] = 0;
    }
}