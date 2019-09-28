package graphic;

import database.dao.AlunoDAO;
import lib.Observable;
import lib.Observer;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import lib.TabelaPadrao;
import database.model.Aluno;
import database.model.MasterModel;

public class PesquisaWindow extends JDialog implements KeyListener, MouseListener, Observable {

    private static final long serialVersionUID = 1L;

    //
    // Cole��o que armazena os observadores desta classe
    //
    private ArrayList<Observer> observers = new ArrayList<Observer>();

    private JPanel pn_principal;

    private JLabel lb_descricao;

    private JTextField tf_descricao;

    private TabelaPadrao tb_resultado;

    private String[] colunas_tabela;

    private Class<?>[] classes_colunas;

    private int[] largura_colunas;

    private int largura_tabela = 0;

    private List<Object> lista_resultado;
    private Connection conn;

    public PesquisaWindow(AlunoDAO dao, String[] colunas_tabela, Class<?>[] classes_colunas, int[] largura_colunas,
            AlunoWindow alunoWindow, Connection conn) {
        // Seta a referencia do objeto dao para esta classe
        //
        setIconImage(Toolkit.getDefaultToolkit().getImage("icons/logo.png"));
        //
        // Seta a variavel que armazena as colunas da tabela
        //
        this.colunas_tabela = colunas_tabela;

        this.conn = conn;
        
        //
        // Seta a variavel que armazena as classes das colunas da tabela
        //
        this.classes_colunas = classes_colunas;

        //
        // Seta a variavel que armazena as larguras das colunas
        //
        this.largura_colunas = largura_colunas;

        //
        // Seta o observador desta classe
        //
         addObserver(alunoWindow);

        //
        // Metodo para construir os componentes
        //
        montaTela();

        pack();

        //
        // Centraliza a janela
        //
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - (this.getWidth())) / 2, (screenSize.height - (this.getHeight())) / 2);

        //
        // Atributos do JDialog
        //
        setTitle("Pesquisar");
        // setResizable(false);
        setModal(true);

        //
        // Inicializa conte�do da tabela com todos os registros
        //
        try {
            listaResultado(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tb_resultado.tb_padrao.getSelectionModel().setSelectionInterval(0, 0);
    }
    
    public PesquisaWindow(){

    }

    private void montaTela() {

        lb_descricao = new JLabel("Descrição:");
        tf_descricao = new JTextField();
        tf_descricao.addKeyListener(this);

        //
        // Cria a tabela
        //
        tb_resultado = new TabelaPadrao(colunas_tabela, classes_colunas, largura_colunas);
        tb_resultado.tb_padrao.addKeyListener(this);
        tb_resultado.tb_padrao.addMouseListener(this);

        //
        // Soma a largura total das colunas para definir o tamanho dos outros
        // componentes
        //
        for (int i = 0; i < largura_colunas.length; i++) {
            largura_tabela += largura_colunas[i];
        }

        //
        // Define os tamanhos e posi��es dos componentes
        //
        lb_descricao.setBounds(10, 10, 70, 25);
        tf_descricao.setBounds(80, 10, largura_tabela - 70, 25);

        tb_resultado.sp_padrao.setBounds(10, 50, largura_tabela, 250);

        // Cria o painel e adiciona os componentes
        pn_principal = new JPanel(null);
        pn_principal.setPreferredSize(new Dimension(largura_tabela + 20, 310));
        pn_principal.add(lb_descricao);
        pn_principal.add(tf_descricao);
        pn_principal.add(tb_resultado.sp_padrao);

        getContentPane().add(pn_principal);
    }

    /**
     * Consulta na base de dados e mostra na tabela.
     * 
     * @throws SQLException
     */
    private void listaResultado(Connection conn) throws SQLException {

        AlunoDAO connect = new AlunoDAO(conn);
        lista_resultado = connect.Select(null);

        if (!lista_resultado.isEmpty()) {
            for (Object model : lista_resultado) {
                //
                // Adiciona na tabela
                //
               tb_resultado.modelo.addRow(((MasterModel) model).getSearchLine());
            }
        }

    }

    /**
     * Método responsável por controlar a seleção dos registros da tabela através do
     * teclado.
     * 
     * @param tecla Tecla acionada no teclado.
     */
    private void selecionaLinha(int tecla) {
        if (!tb_resultado.tb_padrao.isFocusOwner()) {
            if (tecla == KeyEvent.VK_DOWN) {
                int atual = tb_resultado.tb_padrao.getSelectedRow();

                if (atual < tb_resultado.tb_padrao.getRowCount() - 1) {
                    tb_resultado.tb_padrao.getSelectionModel().setSelectionInterval(atual + 1, atual + 1);
                }
            } else if (tecla == KeyEvent.VK_UP) {
                int atual = tb_resultado.tb_padrao.getSelectedRow();
                if (atual > 0) {
                    tb_resultado.tb_padrao.getSelectionModel().setSelectionInterval(atual - 1, atual - 1);
                }
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            notifyObservers();
            dispose();
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP) {
            selecionaLinha(e.getKeyCode());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() != KeyEvent.VK_UP && e.getKeyCode() != KeyEvent.VK_DOWN) {
            //
            // Remove todas as linhas da tabela
            //
            tb_resultado.modelo.removeAllRows();

            //
            // Se houver texto, faz a busca
            //
             if (tf_descricao.getText().length() > 0){
            try {
				listaResultado(conn);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            // Seta a sele��o na primeira linha
            tb_resultado.tb_padrao.getSelectionModel().setSelectionInterval(0, 0);
             }
        }
    }

    /**
     * Retorna o objeto selecionado da consulta.
     * 
     * @return Objeto selecionado
     */
    Object getObjetoSelecionado() {
        int linha = tb_resultado.tb_padrao.getSelectedRow();

        return lista_resultado.get(linha);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            notifyObservers();
            dispose();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        Iterator<Observer> i = observers.iterator();
        while (i.hasNext()) {
            Observer o = i.next();
            o.update(getObjetoSelecionado());
        }
    }

}
