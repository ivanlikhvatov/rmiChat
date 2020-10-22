package client;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

public class ClientGUI extends JFrame implements ActionListener{

    private ChatClientImpl chatClient;
    private String name;

    private String message;

    private JPanel textPanel, inputPanel;
    private JTextField textField;

    private Font meiryoFont = new Font("Meiryo", Font.PLAIN, 14);
    private Border blankBorder = BorderFactory.createEmptyBorder(10,10,20,10);//top,r,b,l

    private JTextField input;
    private JLabel label = new JLabel("Input:");

    private Container container = this.getContentPane();


    private JPanel startPanel;

    protected JFrame frame = new JFrame();
    protected JPanel clientPanel, userPanel;
    protected JTextArea textArea, userArea;
    protected JButton loginButton, registrationButton, endRegistrationButton, sendGMButton, sendPMessageButton;

    public ClientGUI(){
        super("Простой чат");

        this.setBounds(400,200,550,400);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//        Container container = this.getContentPane();

        container.setLayout(new BorderLayout());
        container.add(getStartPanel(), BorderLayout.CENTER);

    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == endRegistrationButton){
            name = input.getText();

            try{
                chatClient = new ChatClientImpl(this, name);
                chatClient.identificationUser();
            } catch (RemoteException remoteException){
                remoteException.printStackTrace();
            }


        }

        if (e.getSource() == registrationButton){



            container.removeAll();

            container.setLayout(new BorderLayout());
            container.add(getRegistrationPanel(), BorderLayout.CENTER);
            container.revalidate();


        }

    }

    public JPanel getRegistrationPanel(){

        JPanel gridPanel = new JPanel(new SpringLayout());

        SpringLayout layout = new SpringLayout();
        gridPanel.setLayout(layout);

        JLabel nameLabel = new JLabel("Имя: ");
        JTextField nameInput = new JTextField("", 20);

        JLabel passLabel = new JLabel("Пароль: ");
        JTextField passInput = new JTextField("", 20);


        JLabel genderLabel = new JLabel("Укажите пол: ");

        JRadioButton radioMale = new JRadioButton("муж");
        radioMale.setName("male");
        JRadioButton radioFemale = new JRadioButton("жен");
        radioFemale.setName("female");

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(radioMale);
        buttonGroup.add(radioFemale);



        layout.putConstraint(SpringLayout.WEST , nameLabel, 10, SpringLayout.WEST , gridPanel);
        layout.putConstraint(SpringLayout.NORTH, nameLabel, 28, SpringLayout.NORTH, gridPanel);
        layout.putConstraint(SpringLayout.NORTH, nameInput, 25, SpringLayout.NORTH, gridPanel);
        layout.putConstraint(SpringLayout.WEST , nameInput, 62, SpringLayout.EAST , nameLabel);


        layout.putConstraint(SpringLayout.WEST , passLabel, 10, SpringLayout.WEST , gridPanel);
        layout.putConstraint(SpringLayout.NORTH, passLabel, 56, SpringLayout.NORTH, gridPanel);
        layout.putConstraint(SpringLayout.NORTH, passInput, 50, SpringLayout.NORTH, gridPanel);
        layout.putConstraint(SpringLayout.WEST , passInput, 40, SpringLayout.EAST , passLabel);

        layout.putConstraint(SpringLayout.WEST , radioMale, 10, SpringLayout.WEST , gridPanel);
        layout.putConstraint(SpringLayout.NORTH, radioMale, 90, SpringLayout.NORTH, gridPanel);
        layout.putConstraint(SpringLayout.NORTH, radioFemale, 90, SpringLayout.NORTH, gridPanel);
        layout.putConstraint(SpringLayout.WEST , radioFemale, 40, SpringLayout.EAST , radioMale);


        gridPanel.add(nameLabel);
        gridPanel.add(nameInput);
        gridPanel.add(passLabel);
        gridPanel.add(passInput);
        gridPanel.add(radioMale);
        gridPanel.add(radioFemale);

//        JPanel registrationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
//        registrationPanel.add(gridPanel);


        return gridPanel;


    }

    public JPanel getStartPanel(){

        JPanel gridPanel = new JPanel(new GridLayout(1, 2, 5, 0));

        JLabel label = new JLabel("Войдите или зарегистрируйтесь");


        loginButton = new JButton("Войти ");
        loginButton.addActionListener(this);

        registrationButton = new JButton("Зарегистрироваться ");
        registrationButton.addActionListener(this);

        gridPanel.add(loginButton);
        gridPanel.add(registrationButton);


        startPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 70));
        startPanel.add(label);
        startPanel.add(gridPanel);

        return startPanel;
    }

















    public JPanel getInputPanel(){
        inputPanel = new JPanel(new GridLayout(1, 1, 5, 5));
        inputPanel.setBorder(blankBorder);
        textField = new JTextField();
        textField.setFont(meiryoFont);
        inputPanel.add(textField);
        return inputPanel;
    }

    public JPanel getTextPanel(){
        String welcome = "Welcome enter your name, password,\ngender and press Start to begin\n";
        textArea = new JTextArea(welcome, 14, 27);
        textArea.setMargin(new Insets(10, 10, 10, 10));
        textArea.setFont(meiryoFont);

        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        textPanel = new JPanel();
        textPanel.add(scrollPane);

        textPanel.setFont(new Font("Meiryo", Font.PLAIN, 14));
        return textPanel;
    }


    public JPanel makeButtonPanel() {
        sendGMButton = new JButton("Send ");
        sendGMButton.addActionListener(this);
        sendGMButton.setEnabled(false);

        sendPMessageButton = new JButton("Send PM");
        sendPMessageButton.addActionListener(this);
        sendPMessageButton.setEnabled(false);

        loginButton = new JButton("Start ");
        loginButton.addActionListener(this);

        JPanel buttonPanel = new JPanel(new GridLayout(4, 1));
        buttonPanel.add(sendPMessageButton);
        buttonPanel.add(new JLabel(""));
        buttonPanel.add(loginButton);
        buttonPanel.add(sendGMButton);

        return buttonPanel;
    }

    public JPanel getUsersPanel(){

        userPanel = new JPanel(new BorderLayout());
        String  userStr = " Current Users      ";

        JLabel userLabel = new JLabel(userStr, JLabel.CENTER);
        userPanel.add(userLabel, BorderLayout.NORTH);
        userLabel.setFont(new Font("Meiryo", Font.PLAIN, 16));

        String[] noClientsYet = {"No other users"};
        setClientPanel(noClientsYet);

        clientPanel.setFont(meiryoFont);
        userPanel.add(makeButtonPanel(), BorderLayout.SOUTH);
        userPanel.setBorder(blankBorder);

        return userPanel;
    }


    public void setClientPanel(String[] currClients) {
        clientPanel = new JPanel(new BorderLayout());
//        listModel = new DefaultListModel<String>();
//
//        for(String s : currClients){
//            listModel.addElement(s);
//        }
//        if(currClients.length > 1){
//            privateMsgButton.setEnabled(true);
//        }
//
//        //Create the list and put it in a scroll pane.
//        list = new JList<String>(listModel);
//        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//        list.setVisibleRowCount(8);
//        list.setFont(meiryoFont);
//        JScrollPane listScrollPane = new JScrollPane(list);

//        clientPanel.add(listScrollPane, BorderLayout.CENTER);
        userPanel.add(clientPanel, BorderLayout.CENTER);
    }


    public static void main(String[] args) throws Exception {
        ClientGUI app = new ClientGUI();
        app.setVisible(true);


    }
}
