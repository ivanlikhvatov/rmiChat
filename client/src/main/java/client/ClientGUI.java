package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;

import static client.ChatClientImpl.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientGUI extends JFrame implements ActionListener{

    private ChatClientImpl chatClient;
    private String name;
    private char[] pass;
    private String gender;
    protected String login; //TODO подумать стоит ли тут размещать логин
    private UserLoginAndName addressee;

    protected JFrame frame;
    private Container container = this.getContentPane();
    protected List<PrivateMessage> privateMessages;
    protected List<String> generalMessages;
    private JList<UserLoginAndName> activeUsers;
    protected JList<DialogLastMessage> privateDialogs;
    private JPanel inputPanel;
    protected JTextField nameInput, messageInput;
    private JPasswordField passInput;
    private JRadioButton radioMale, radioFemale;
    protected JButton loginButton, logoutButton, registrationButton, endRegistrationButton,
            sendGMButton, sendPMessageButton,
            getStartPanelButton, getGMPanelButton, getPMPanelButton, getPMDialogPanelButton, openPrivateChat;
    protected JPanel activeUsersScrollPanel, pmDialogsScrollPanel, generalMessagePanel, privateMessagePanel, PMDialogPanel;
    protected JTextArea generalTextArea, privateTextArea;

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

                chatClient.identificationUser();

                container.removeAll();
                container.setLayout(new BorderLayout());
                container.add(getGeneralMessagePanel(), BorderLayout.CENTER);
                container.revalidate();


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

        if (e.getSource() == getGMPanelButton){
            container.removeAll();
            container.setLayout(new BorderLayout());
            container.add(getGeneralMessagePanel(), BorderLayout.CENTER);
            container.revalidate();
        }

        if (e.getSource() == getPMPanelButton){
            container.removeAll();
            container.setLayout(new BorderLayout());
            container.add(getPrivateMessagePanel(), BorderLayout.CENTER);
            container.revalidate();
        }


        if(e.getSource() == openPrivateChat){
            if (activeUsers.getSelectedIndices().length > 1){
                List<String> loginList = new ArrayList<>();

                for (UserLoginAndName user : activeUsers.getSelectedValuesList()){
                    loginList.add(user.getLogin());
                }

                try{
                    chatClient.sendPrivateMessage(loginList, messageInput.getText());
                } catch (RemoteException remoteException){
                    remoteException.printStackTrace();
                }

            } else {
                addressee = activeUsers.getSelectedValue();

                container.removeAll();
                container.setLayout(new BorderLayout());
                container.add(getPrivateDialogPanel(), BorderLayout.CENTER);
                container.revalidate();
            }


        }

        if (e.getSource() == sendPMessageButton){
            if (activeUsers.getSelectedIndices().length > 1){
                List<String> loginList = new ArrayList<>();

                for (UserLoginAndName user : activeUsers.getSelectedValuesList()){
                    loginList.add(user.getLogin());
                }

                try{
                    chatClient.sendPrivateMessage(loginList, messageInput.getText());
                } catch (RemoteException remoteException){
                    remoteException.printStackTrace();
                }


            } else {
                UserLoginAndName addressee = activeUsers.getSelectedValue();

                try{
                    chatClient.sendPrivateMessage(addressee.getLogin(), messageInput.getText());
                } catch (RemoteException remoteException){
                    remoteException.printStackTrace();
                }
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
        generalMessagePanel.add(getGeneralTextPanel(), BorderLayout.CENTER);


        generalMessagePanel.add(activeUsersScrollPanel, BorderLayout.WEST);

//        generalMessagePanel.setBorder(blankBorder);

        return generalMessagePanel;
    }

    public JPanel getPrivateMessagePanel(){

        privateMessagePanel = new JPanel(new BorderLayout());

        getGMPanelButton = new JButton("общие сообщения");
        getGMPanelButton.addActionListener(this);

        getPMPanelButton = new JButton("личные сообщения");
        getPMPanelButton.addActionListener(this);

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

        if (privateDialogs.getModel().getSize() == 0){
            List<String[]> emptyDialogs = new ArrayList<>();
            emptyDialogs.add(new String[]{"nothing", "У вас нет сообщений", ""});
            setPrivateDialogsPanel(emptyDialogs);
        }



        privateMessagePanel.add(flowLeftAndRight, BorderLayout.NORTH);
        privateMessagePanel.add(activeUsersScrollPanel, BorderLayout.WEST);
        privateMessagePanel.add(pmDialogsScrollPanel, BorderLayout.CENTER);

        return privateMessagePanel;
    }

    public JPanel getPrivateDialogPanel(){
        PMDialogPanel = new JPanel(new BorderLayout());

        return PMDialogPanel;
    }

    public void setPrivateDialogsPanel(List<String[]> interlocutorsAndLastMessage){

        pmDialogsScrollPanel = new JPanel(new BorderLayout());

        DefaultListModel<DialogLastMessage> tempPrivateMessages = new DefaultListModel<>();

        for (String[] messageDetails : interlocutorsAndLastMessage){
            String login = messageDetails[0];
            String username = messageDetails[1];
            String message = messageDetails[2];

            UserLoginAndName user = new UserLoginAndName(login, username);

            tempPrivateMessages.addElement(new DialogLastMessage(user, message));
        }

        privateDialogs = new JList<>(tempPrivateMessages);
        privateDialogs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        privateDialogs.setVisibleRowCount(2);
        privateDialogs.setFixedCellHeight(44);


        JScrollPane listScrollPane = new JScrollPane(privateDialogs);

        JLabel dialogLabel = new JLabel("Мои диалоги", JLabel.CENTER);
        dialogLabel.setFont(new Font("Meiryo", Font.PLAIN, 16));
        dialogLabel.setBorder(new EmptyBorder(0, 3, 6, 3));

        pmDialogsScrollPanel.add(dialogLabel, BorderLayout.NORTH);

        pmDialogsScrollPanel.add(listScrollPane, BorderLayout.CENTER);
        pmDialogsScrollPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 30, 20));

        if (privateMessagePanel != null){
            privateMessagePanel.add(pmDialogsScrollPanel, BorderLayout.CENTER);
        }
    }

    public void setPrivateMessages(Map<String, String> messagesAndAuthor){

    }

    public void setActiveUsersPanel(Map<String, String> users){

        activeUsersScrollPanel = new JPanel(new BorderLayout());

        DefaultListModel<UserLoginAndName> tempUsersList = new DefaultListModel<>();


        for (Map.Entry<String, String> entry : users.entrySet()) {
            tempUsersList.addElement(new UserLoginAndName(entry.getKey(), entry.getValue()));
        }

        activeUsers = new JList<>(tempUsersList);
        activeUsers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        activeUsers.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                if (!activeUsers.isSelectionEmpty()){
                    if (activeUsers.getSelectedIndices().length > 1){
                        openPrivateChat.setText("отправить выбранным");
                        openPrivateChat.setMargin(new Insets(5, -2, 5, -2));
                    }

                    if (activeUsers.getSelectedIndices().length <= 1){
                        openPrivateChat.setText("написать");
                        openPrivateChat.setMargin(new Insets(5, 0, 5, 0));

                    }
                }
            }
        });
        activeUsers.setVisibleRowCount(8);
        JScrollPane listScrollPane = new JScrollPane(activeUsers);

        String  userStr = "Пользователи онлайн";
        openPrivateChat = new JButton("написать");
        openPrivateChat.addActionListener(this);

        openPrivateChat.setMargin(new Insets(5, 0, 5, 0));

        JLabel userLabel = new JLabel(userStr, JLabel.CENTER);
        userLabel.setBorder(new EmptyBorder(0, 3, 6, 3));

        activeUsersScrollPanel.add(userLabel, BorderLayout.NORTH);
        activeUsersScrollPanel.add(openPrivateChat, BorderLayout.SOUTH);

        userLabel.setFont(new Font("Meiryo", Font.PLAIN, 16));

        activeUsersScrollPanel.add(listScrollPane, BorderLayout.CENTER);
        activeUsersScrollPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 30, 0));

        if (generalMessagePanel != null){
            generalMessagePanel.add(activeUsersScrollPanel, BorderLayout.WEST);
        }

        if (privateMessagePanel != null){
            privateMessagePanel.add(activeUsersScrollPanel, BorderLayout.WEST);
        }

    }



    public JPanel getGeneralTextPanel(){
        String welcome = "Приветствуем вас в нашем чате!\n";
        generalTextArea = new JTextArea(welcome, 16, 42);
        generalTextArea.setMargin(new Insets(10, 10, 10, 10));

        generalTextArea.setLineWrap(true);
        generalTextArea.setWrapStyleWord(true);
        generalTextArea.setEditable(false);


        for (int i = 0; i < generalMessages.size(); i++){
            generalTextArea.append(generalMessages.get(i));
        }


        JScrollPane scrollPane = new JScrollPane(generalTextArea);
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

    public UserLoginAndName findActiveByLogin(String login){

        for (UserLoginAndName user : activeUsers.getSelectedValuesList()){
            if (user.getLogin().equals(login)){
                return user;
            }
        }

        return null;
    }

    public static void main(String[] args){

        ClientGUI app = new ClientGUI();
        app.setVisible(true);

    }
}
