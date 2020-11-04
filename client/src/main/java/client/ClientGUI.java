package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;

import static client.ChatClientImpl.*;

import java.util.HashMap;
import java.util.Map;

public class ClientGUI extends JFrame implements ActionListener{

    private ChatClientImpl chatClient;
    private String name;
    private char[] pass;
    private String gender;

    protected String login; //TODO подумать стоит ли тут размещать логин

    private String message;

    private JList<UserLoginAndName> activeUsers;


    private Container container = this.getContentPane();



    protected JFrame frame;

    private JPanel inputPanel;
    protected JTextField nameInput, messageInput;
    private JPasswordField passInput;
    private JRadioButton radioMale, radioFemale;
    protected JButton loginButton, logoutButton, registrationButton, endRegistrationButton,
            sendGMButton, sendPMessageButton,
            getStartPanelButton, getGMPanelButton, getPMPanelButton, getPMDialogPanelButton;
    protected JPanel activeUsersScrollPanel, generalMessagePanel, privateMessagePanel, PMDialogPanel;
    protected JTextArea textArea;

    public ClientGUI(){
        super("Простой чат");

        this.setBounds(400,200,550,400);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container container = this.getContentPane();

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {

                if(chatClient != null){
                    try {
                        chatClient.chatServer.disconnect(chatClient);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                System.exit(0);
            }
        });



        container.setLayout(new BorderLayout());
        container.add(getStartPanel(), BorderLayout.CENTER);

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == registrationButton){
            container.removeAll();
            container.setLayout(new BorderLayout());
            container.add(getRegistrationPanel(), BorderLayout.CENTER);
            container.revalidate();
        }

        if (e.getSource() == getStartPanelButton){
            container.removeAll();
            container.setLayout(new BorderLayout());
            container.add(getStartPanel(), BorderLayout.CENTER);
            container.revalidate();
        }

        if (e.getSource() == endRegistrationButton){


            //TODO выкидыает ошибку и регистрирует с null-ом того поля которое не заполнено
            if (nameInput.getText().isEmpty() || passInput.getPassword().length == 0 || (!radioFemale.isSelected() && !radioMale.isSelected())){
                JOptionPane.showMessageDialog(null, "Заполните все поля!",
                        "Не все поля заполнены", JOptionPane.ERROR_MESSAGE);
            }

            name = nameInput.getText();
            pass = passInput.getPassword();

            if (radioMale.isSelected()){
                gender = radioMale.getName();
            } else if (radioFemale.isSelected()){
                gender = radioFemale.getText();
            }

            try{
                chatClient = new ChatClientImpl(this, name, gender, pass);

                container.removeAll();
                container.setLayout(new BorderLayout());
                container.add(getGeneralMessagePanel(), BorderLayout.CENTER);
                container.revalidate();


                chatClient.identificationUser();

                this.setBounds(300,200,750,400);

            } catch (RemoteException remoteException){
                remoteException.printStackTrace();
            }


        }


        if (e.getSource() == logoutButton){

            if(chatClient != null){
                try {
                    chatClient.chatServer.disconnect(chatClient);

                    container.removeAll();
                    container.setLayout(new BorderLayout());
                    container.add(getStartPanel(), BorderLayout.CENTER);
                    container.revalidate();
                    this.setBounds(400,200,550,400);

                } catch (RemoteException remoteException) {
                    remoteException.printStackTrace();
                }
            }
        }

        if (e.getSource() == sendGMButton){

            try{
                chatClient.sendGeneralMessage(messageInput.getText(), login);
            } catch (RemoteException remoteException){
                remoteException.printStackTrace();
            }

        }

    }

    public JPanel getRegistrationPanel(){

        JPanel registrationPanel = new JPanel(new SpringLayout());

        SpringLayout layout = new SpringLayout();
        registrationPanel.setLayout(layout);

        JLabel nameLabel = new JLabel("Имя: ");
        nameInput = new JTextField("", 20);

        JLabel passLabel = new JLabel("Пароль: ");
        passInput = new JPasswordField("", 20);


        JLabel genderLabel = new JLabel("Укажите пол: ");

        radioMale = new JRadioButton("муж");
        radioMale.setName("male");
        radioFemale = new JRadioButton("жен");
        radioFemale.setName("female");

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(radioMale);
        buttonGroup.add(radioFemale);

        endRegistrationButton = new JButton("Регистрация");
        endRegistrationButton.addActionListener(this);
        getStartPanelButton = new JButton("Вернуться в главное меню");
        getStartPanelButton.addActionListener(this);

        layout.putConstraint(SpringLayout.WEST , nameLabel, 10, SpringLayout.WEST , registrationPanel);
        layout.putConstraint(SpringLayout.NORTH, nameLabel, 28, SpringLayout.NORTH, registrationPanel);
        layout.putConstraint(SpringLayout.NORTH, nameInput, 25, SpringLayout.NORTH, registrationPanel);
        layout.putConstraint(SpringLayout.WEST , nameInput, 62, SpringLayout.EAST , nameLabel);


        layout.putConstraint(SpringLayout.WEST , passLabel, 10, SpringLayout.WEST , registrationPanel);
        layout.putConstraint(SpringLayout.NORTH, passLabel, 56, SpringLayout.NORTH, registrationPanel);
        layout.putConstraint(SpringLayout.NORTH, passInput, 50, SpringLayout.NORTH, registrationPanel);
        layout.putConstraint(SpringLayout.WEST , passInput, 40, SpringLayout.EAST , passLabel);

        layout.putConstraint(SpringLayout.WEST , genderLabel, 10, SpringLayout.WEST , registrationPanel);
        layout.putConstraint(SpringLayout.NORTH, genderLabel, 90, SpringLayout.NORTH, registrationPanel);
        layout.putConstraint(SpringLayout.NORTH, radioMale, 88, SpringLayout.NORTH, registrationPanel);
        layout.putConstraint(SpringLayout.WEST , radioMale, 30, SpringLayout.EAST , genderLabel);
        layout.putConstraint(SpringLayout.NORTH, radioFemale, 88, SpringLayout.NORTH, registrationPanel);
        layout.putConstraint(SpringLayout.WEST , radioFemale, 90, SpringLayout.EAST , genderLabel);

        layout.putConstraint(SpringLayout.NORTH, endRegistrationButton, 140, SpringLayout.NORTH, registrationPanel);
        layout.putConstraint(SpringLayout.NORTH, getStartPanelButton, 140, SpringLayout.NORTH, registrationPanel);
        layout.putConstraint(SpringLayout.WEST , getStartPanelButton, 5, SpringLayout.EAST , endRegistrationButton);

        registrationPanel.add(nameLabel);
        registrationPanel.add(nameInput);
        registrationPanel.add(passLabel);
        registrationPanel.add(passInput);
        registrationPanel.add(genderLabel);
        registrationPanel.add(radioMale);
        registrationPanel.add(radioFemale);
        registrationPanel.add(endRegistrationButton);
        registrationPanel.add(getStartPanelButton);


        return registrationPanel;
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


        JPanel startPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 70));
        startPanel.add(label);
        startPanel.add(gridPanel);

        return startPanel;
    }

    public JPanel getGeneralMessagePanel(){

        generalMessagePanel = new JPanel(new BorderLayout());

        getGMPanelButton = new JButton("общие сообщения");
        getGMPanelButton.addActionListener(this);

        getPMPanelButton = new JButton("личные сообщения");
        getPMPanelButton.addActionListener(this);

        sendGMButton = new JButton("отправить");
        sendGMButton.setMargin(new Insets(5, 10, 5, 10));
        sendGMButton.addActionListener(this);

        logoutButton = new JButton("выйти");
        logoutButton.addActionListener(this);


        JPanel chooseTypeMessagePanel = new JPanel(new GridLayout(1, 2, 28, 0));
        chooseTypeMessagePanel.add(getGMPanelButton);
        chooseTypeMessagePanel.add(getPMPanelButton);

        JPanel flowLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
        flowLeft.add(chooseTypeMessagePanel);
        flowLeft.setBorder(new EmptyBorder(5, 0, -5, 0));



        JPanel logoutPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        logoutPanel.add(logoutButton);

        JPanel flowRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        flowRight.add(logoutPanel);
        flowRight.setBorder(new EmptyBorder(5, 0, -5, 0));


        JPanel flowLeftAndRight = new JPanel(new GridLayout(1, 2, 5, 0));

        flowLeftAndRight.add(flowLeft);
        flowLeftAndRight.add(flowRight);

        generalMessagePanel.add(flowLeftAndRight, BorderLayout.NORTH);
//        generalMessagePanel.add(flowRight, BorderLayout.NORTH);

        generalMessagePanel.add(getInputPanel(), BorderLayout.SOUTH);
        inputPanel.add(sendGMButton, BorderLayout.WEST);
        generalMessagePanel.add(getTextPanel(), BorderLayout.CENTER);

        Map<String, String> temp = new HashMap<>();
        temp.put("nothing", "noChatters");
        setActiveUsersPanel(temp);

//        generalMessagePanel.setBorder(blankBorder);

        return generalMessagePanel;
    }

    public JPanel getPrivateMessagePanel(){

        return privateMessagePanel;
    }

    public JPanel getPMDialogPanel(){

        return PMDialogPanel;
    }

    public void setActiveUsersPanel(Map<String, String> users){

        DefaultListModel<UserLoginAndName> tempUsersList = new DefaultListModel<>();
        activeUsersScrollPanel = new JPanel(new BorderLayout());

        for (Map.Entry<String, String> entry : users.entrySet()) {
            tempUsersList.addElement(new UserLoginAndName(entry.getKey(), entry.getValue()));
        }

        activeUsers = new JList<>(tempUsersList);
        activeUsers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        activeUsers.setVisibleRowCount(8);
        JScrollPane listScrollPane = new JScrollPane(activeUsers);

        String  userStr = "Пользователи онлайн";
        sendPMessageButton = new JButton("написать");

        sendPMessageButton.setMargin(new Insets(5, 0, 5, 0));

        JLabel userLabel = new JLabel(userStr, JLabel.CENTER);
        userLabel.setBorder(new EmptyBorder(0, 3, 6, 3));

        activeUsersScrollPanel.add(userLabel, BorderLayout.NORTH);
        activeUsersScrollPanel.add(sendPMessageButton, BorderLayout.SOUTH);

        userLabel.setFont(new Font("Meiryo", Font.PLAIN, 16));

        activeUsersScrollPanel.add(listScrollPane, BorderLayout.CENTER);
        activeUsersScrollPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 30, 0));

        if (generalMessagePanel != null){
            generalMessagePanel.add(activeUsersScrollPanel, BorderLayout.WEST);
        }

        if (privateMessagePanel != null){
            privateMessagePanel.add(activeUsersScrollPanel, BorderLayout.WEST);
        }

        if (PMDialogPanel != null){
            PMDialogPanel.add(activeUsersScrollPanel, BorderLayout.WEST);
        }

    }

    public JPanel getTextPanel(){
        String welcome = "Приветствуем вас в нашем чате!\n";
        textArea = new JTextArea(welcome, 16, 42);
        textArea.setMargin(new Insets(10, 10, 10, 10));
//        textArea.setFont(meiryoFont);

        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        JPanel textPanel = new JPanel();
        textPanel.add(scrollPane);

        textPanel.setFont(new Font("Meiryo", Font.PLAIN, 14));
        return textPanel;
    }

    public JPanel getInputPanel(){
        inputPanel = new JPanel(new BorderLayout());
//        inputPanel.setBorder(blankBorder);
        messageInput = new JTextField();
//        textField.setFont(meiryoFont);
        inputPanel.add(messageInput, BorderLayout.CENTER);

        inputPanel.setBorder(new EmptyBorder(10, 8, 10, 8));
        return inputPanel;
    }



    public static void main(String[] args) throws Exception {

        ClientGUI app = new ClientGUI();
        app.setVisible(true);

    }
}
