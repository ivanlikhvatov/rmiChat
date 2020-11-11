package client;

import interfaces.ChatClient;

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
    protected JTextField nameInput, messageInput, loginInput;
    private JPasswordField passInput;
    private JRadioButton radioMale, radioFemale;
    protected JButton getLoginPanelButton, loginButton, logoutButton, getRegistrationPanelButton, registrationButton,
            sendGMButton, sendPMessageButton,
            getStartPanelButton, getGMPanelButton, getDialogsPanelButton, openPrivateChatButton;
    protected JPanel activeUsersScrollPanel, pmDialogsScrollPanel, generalMessagePanel, dialogsPanel, privateMessagePanel;
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

        if (e.getSource() == getRegistrationPanelButton){
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

        if (e.getSource() == registrationButton){

            if (nameInput.getText().isEmpty() || passInput.getPassword().length == 0 || (!radioFemale.isSelected() && !radioMale.isSelected())){
                JOptionPane.showMessageDialog(null, "Заполните все поля!",
                        "Не все поля заполнены", JOptionPane.ERROR_MESSAGE);
                return;
            }

            name = nameInput.getText();
            pass = passInput.getPassword();

            if (radioMale.isSelected()){
                gender = radioMale.getName();
            } else if (radioFemale.isSelected()){
                gender = radioFemale.getName();
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

        if (e.getSource() == loginButton){
            if (loginInput.getText().isEmpty() || passInput.getPassword().length == 0 ){
                JOptionPane.showMessageDialog(null, "Заполните все поля!",
                        "Не все поля заполнены", JOptionPane.ERROR_MESSAGE);
                return;
            }
            pass = passInput.getPassword();

            try{
                chatClient = new ChatClientImpl(this, null, null, pass);
                chatClient.checkLoggingInUser(loginInput.getText(), pass);
            } catch (RemoteException remoteException){
                remoteException.printStackTrace();
            }





        }

        if (e.getSource() == getLoginPanelButton){
            container.removeAll();
            container.setLayout(new BorderLayout());
            container.add(getLoginPanel(), BorderLayout.CENTER);
            container.revalidate();
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

        if (e.getSource() == getDialogsPanelButton){
            container.removeAll();
            container.setLayout(new BorderLayout());
            container.add(getDialogsPanel(), BorderLayout.CENTER);
            container.revalidate();
        }


        if(e.getSource() == openPrivateChatButton){
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

            } else if (activeUsers.getSelectedIndices().length != 0){
                addressee = activeUsers.getSelectedValue();

                container.removeAll();
                container.setLayout(new BorderLayout());
                container.add(getPrivateMessagePanel(), BorderLayout.CENTER);
                container.revalidate();
            } else if (privateDialogs.getSelectedIndices().length != 0){
                addressee = privateDialogs.getSelectedValue().getInterlocutor();

                container.removeAll();
                container.setLayout(new BorderLayout());
                container.add(getPrivateMessagePanel(), BorderLayout.CENTER);
                container.revalidate();
            }


        }

        if (e.getSource() == sendPMessageButton){

            try{
                chatClient.sendPrivateMessage(addressee.getLogin(), messageInput.getText());
            } catch (RemoteException remoteException){
                remoteException.printStackTrace();
            }

        }

    }
    public JPanel getStartPanel(){

        JPanel gridPanel = new JPanel(new GridLayout(1, 2, 5, 0));

        JLabel label = new JLabel("Войдите или зарегистрируйтесь");


        getLoginPanelButton = new JButton("Войти ");
        getLoginPanelButton.addActionListener(this);

        getRegistrationPanelButton = new JButton("Зарегистрироваться ");
        getRegistrationPanelButton.addActionListener(this);

        gridPanel.add(getLoginPanelButton);
        gridPanel.add(getRegistrationPanelButton);


        JPanel startPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 70));
        startPanel.add(label);
        startPanel.add(gridPanel);

        return startPanel;
    }

    public JPanel getLoginPanel(){
        JPanel loginPanel = new JPanel(new BorderLayout());

        SpringLayout layout = new SpringLayout();
        loginPanel.setLayout(layout);

        JLabel loginLabel = new JLabel("Логин: ");
        loginInput = new JTextField("", 20);

        JLabel passLabel = new JLabel("Пароль: ");
        passInput = new JPasswordField("", 20);

        loginButton = new JButton("войти");
        loginButton.addActionListener(this);

        getStartPanelButton = new JButton("вернуться в главное меню");
        getStartPanelButton.addActionListener(this);

        layout.putConstraint(SpringLayout.WEST , loginLabel, 10, SpringLayout.WEST , loginPanel);
        layout.putConstraint(SpringLayout.NORTH, loginLabel, 28, SpringLayout.NORTH, loginPanel);
        layout.putConstraint(SpringLayout.NORTH, loginInput, 25, SpringLayout.NORTH, loginPanel);
        layout.putConstraint(SpringLayout.WEST , loginInput, 49, SpringLayout.EAST , loginLabel);

        layout.putConstraint(SpringLayout.WEST , passLabel, 10, SpringLayout.WEST , loginPanel);
        layout.putConstraint(SpringLayout.NORTH, passLabel, 56, SpringLayout.NORTH, loginPanel);
        layout.putConstraint(SpringLayout.NORTH, passInput, 50, SpringLayout.NORTH, loginPanel);
        layout.putConstraint(SpringLayout.WEST , passInput, 40, SpringLayout.EAST , passLabel);

        layout.putConstraint(SpringLayout.NORTH, loginButton, 100, SpringLayout.NORTH, loginPanel);
        layout.putConstraint(SpringLayout.NORTH, getStartPanelButton, 100, SpringLayout.NORTH, loginPanel);
        layout.putConstraint(SpringLayout.WEST , getStartPanelButton, 5, SpringLayout.EAST , loginButton);

        loginPanel.add(loginLabel);
        loginPanel.add(loginInput);
        loginPanel.add(passLabel);
        loginPanel.add(passInput);

        loginPanel.add(loginButton);
        loginPanel.add(getStartPanelButton);


        return loginPanel;
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

        registrationButton = new JButton("регистрация");
        registrationButton.addActionListener(this);
        getStartPanelButton = new JButton("вернуться в главное меню");
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

        layout.putConstraint(SpringLayout.NORTH, registrationButton, 140, SpringLayout.NORTH, registrationPanel);
        layout.putConstraint(SpringLayout.NORTH, getStartPanelButton, 140, SpringLayout.NORTH, registrationPanel);
        layout.putConstraint(SpringLayout.WEST , getStartPanelButton, 5, SpringLayout.EAST , registrationButton);

        registrationPanel.add(nameLabel);
        registrationPanel.add(nameInput);
        registrationPanel.add(passLabel);
        registrationPanel.add(passInput);
        registrationPanel.add(genderLabel);
        registrationPanel.add(radioMale);
        registrationPanel.add(radioFemale);
        registrationPanel.add(registrationButton);
        registrationPanel.add(getStartPanelButton);


        return registrationPanel;
    }

    public JPanel getGeneralMessagePanel(){

        generalMessagePanel = new JPanel(new BorderLayout());

        getGMPanelButton = new JButton("общие сообщения");
        getGMPanelButton.addActionListener(this);

        getDialogsPanelButton = new JButton("личные сообщения");
        getDialogsPanelButton.addActionListener(this);

        sendGMButton = new JButton("отправить");
        sendGMButton.setMargin(new Insets(5, 10, 5, 10));
        sendGMButton.addActionListener(this);

        logoutButton = new JButton("выйти");
        logoutButton.addActionListener(this);


        JPanel chooseTypeMessagePanel = new JPanel(new GridLayout(1, 2, 28, 0));
        chooseTypeMessagePanel.add(getGMPanelButton);
        chooseTypeMessagePanel.add(getDialogsPanelButton);

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
        generalMessagePanel.add(getInputPanel(), BorderLayout.SOUTH);
        inputPanel.add(sendGMButton, BorderLayout.WEST);
        generalMessagePanel.add(getGeneralTextPanel(), BorderLayout.CENTER);
        generalMessagePanel.add(activeUsersScrollPanel, BorderLayout.WEST);


        activeUsers.removeSelectionInterval(0, activeUsers.getModel().getSize() - 1);
        privateDialogs.removeSelectionInterval(0, privateDialogs.getModel().getSize() - 1);
        activeUsers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        return generalMessagePanel;
    }

    public JPanel getDialogsPanel(){

        dialogsPanel = new JPanel(new BorderLayout());

        getGMPanelButton = new JButton("общие сообщения");
        getGMPanelButton.addActionListener(this);

        getDialogsPanelButton = new JButton("личные сообщения");
        getDialogsPanelButton.addActionListener(this);

        logoutButton = new JButton("выйти");
        logoutButton.addActionListener(this);


        JPanel chooseTypeMessagePanel = new JPanel(new GridLayout(1, 2, 28, 0));
        chooseTypeMessagePanel.add(getGMPanelButton);
        chooseTypeMessagePanel.add(getDialogsPanelButton);

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

        activeUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        dialogsPanel.add(flowLeftAndRight, BorderLayout.NORTH);
        dialogsPanel.add(activeUsersScrollPanel, BorderLayout.WEST);
        dialogsPanel.add(pmDialogsScrollPanel, BorderLayout.CENTER);

        activeUsers.removeSelectionInterval(0, activeUsers.getModel().getSize() - 1);
        privateDialogs.removeSelectionInterval(0, privateDialogs.getModel().getSize() - 1);

        return dialogsPanel;
    }

    public JPanel getPrivateMessagePanel(){
        privateMessagePanel = new JPanel(new BorderLayout());

        sendPMessageButton = new JButton("отправить");
        sendPMessageButton.setMargin(new Insets(5, 10, 5, 10));
        sendPMessageButton.addActionListener(this);

        getGMPanelButton = new JButton("общие сообщения");
        getGMPanelButton.addActionListener(this);

        getDialogsPanelButton = new JButton("личные сообщения");
        getDialogsPanelButton.addActionListener(this);

        logoutButton = new JButton("выйти");
        logoutButton.addActionListener(this);


        JPanel chooseTypeMessagePanel = new JPanel(new GridLayout(1, 2, 28, 0));
        chooseTypeMessagePanel.add(getGMPanelButton);
        chooseTypeMessagePanel.add(getDialogsPanelButton);

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

        privateMessagePanel.add(getInputPanel(), BorderLayout.SOUTH);
        inputPanel.add(sendPMessageButton, BorderLayout.WEST);
        privateMessagePanel.add(flowLeftAndRight, BorderLayout.NORTH);
        privateMessagePanel.add(getPrivateTextPanel(), BorderLayout.CENTER);

        return privateMessagePanel;
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
        privateDialogs.setCellRenderer(getRenderer());

        JScrollPane listScrollPane = new JScrollPane(privateDialogs);
        listScrollPane.setHorizontalScrollBarPolicy(listScrollPane.HORIZONTAL_SCROLLBAR_NEVER);


        JLabel dialogLabel = new JLabel("Мои диалоги", JLabel.CENTER);
        dialogLabel.setFont(new Font("Meiryo", Font.PLAIN, 16));
        dialogLabel.setBorder(new EmptyBorder(0, 3, 6, 3));

        pmDialogsScrollPanel.add(dialogLabel, BorderLayout.NORTH);

        pmDialogsScrollPanel.add(listScrollPane, BorderLayout.CENTER);
        pmDialogsScrollPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 30, 20));



        if (dialogsPanel != null){
            dialogsPanel.add(pmDialogsScrollPanel, BorderLayout.CENTER);
        }


        privateDialogs.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                if (!privateDialogs.isSelectionEmpty()){

                    if (activeUsers != null && activeUsers.getSelectedIndices().length != 0){
                        activeUsers.removeSelectionInterval(0, activeUsers.getModel().getSize());
                        openPrivateChatButton.setText("написать");
                        openPrivateChatButton.setMargin(new Insets(5, 0, 5, 0));
                    }

                }
            }
        });
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
                        openPrivateChatButton.setText("отправить выбранным");
                        openPrivateChatButton.setMargin(new Insets(5, -2, 5, -2));
                    }

                    if (privateDialogs != null && privateDialogs.getSelectedIndices().length != 0){
                        privateDialogs.removeSelectionInterval(0, privateDialogs.getModel().getSize());
                    }

                    if (activeUsers.getSelectedIndices().length <= 1){
                        openPrivateChatButton.setText("написать");
                        openPrivateChatButton.setMargin(new Insets(5, 0, 5, 0));

                    }

                    if (activeUsers.getSelectedValue().getLogin().equals(login)){

                        activeUsers.removeSelectionInterval(activeUsers.getSelectedIndex(), activeUsers.getSelectedIndex());
                    }

                    //TODO убрать возможность выбора самого себя

//                    for (UserLoginAndName userLoginAndName : activeUsers.getSelectedValuesList()){
//                        if (userLoginAndName.getLogin().equals(login)){
//                            activeUsers.removeSelectionInterval(activeUsers.getComponent(1));
//                        }
//                    }


                    for (int i = 1; i < activeUsers.getModel().getSize(); i++){
                        if (activeUsers.getModel().getElementAt(i).getLogin().equals(login)){
                            activeUsers.removeSelectionInterval(i, i);
                        }
                    }

                }
            }
        });
        activeUsers.setVisibleRowCount(8);
        JScrollPane listScrollPane = new JScrollPane(activeUsers);

        String  userStr = "Пользователи онлайн";
        openPrivateChatButton = new JButton("написать");
        openPrivateChatButton.addActionListener(this);

        openPrivateChatButton.setMargin(new Insets(5, 0, 5, 0));

        JLabel userLabel = new JLabel(userStr, JLabel.CENTER);
        userLabel.setBorder(new EmptyBorder(0, 3, 6, 3));

        activeUsersScrollPanel.add(userLabel, BorderLayout.NORTH);
        activeUsersScrollPanel.add(openPrivateChatButton, BorderLayout.SOUTH);

        userLabel.setFont(new Font("Meiryo", Font.PLAIN, 16));

        activeUsersScrollPanel.add(listScrollPane, BorderLayout.CENTER);
        activeUsersScrollPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 30, 0));

        if (generalMessagePanel != null){
            generalMessagePanel.add(activeUsersScrollPanel, BorderLayout.WEST);
        }

        if (dialogsPanel != null){
            dialogsPanel.add(activeUsersScrollPanel, BorderLayout.WEST);
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

    public JPanel getPrivateTextPanel(){

        privateTextArea = new JTextArea( 16, 42);
        privateTextArea.setMargin(new Insets(10, 10, 10, 10));

        privateTextArea.setLineWrap(true);
        privateTextArea.setWrapStyleWord(true);
        privateTextArea.setEditable(false);


        for (PrivateMessage privateMessage : privateMessages) {
            if (privateMessage.getSender().getLogin().equals(addressee.getLogin()) || privateMessage.getAddressee().getLogin().equals(addressee.getLogin())) {
                privateTextArea.append(privateMessage.getText());
            }
        }


        JScrollPane scrollPane = new JScrollPane(privateTextArea);
        JPanel textPanel = new JPanel();
        textPanel.add(scrollPane);

        textPanel.setFont(new Font("Meiryo", Font.PLAIN, 14));
        return textPanel;
    }

    public JPanel getInputPanel(){
        inputPanel = new JPanel(new BorderLayout());
        messageInput = new JTextField();
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

    private ListCellRenderer<? super DialogLastMessage> getRenderer() {
        return new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                                                          Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel listCellRendererComponent = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,cellHasFocus);
                listCellRendererComponent.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,Color.BLACK));
                return listCellRendererComponent;
            }
        };
    }

    public static void main(String[] args){

        ClientGUI app = new ClientGUI();
        app.setVisible(true);

    }
}
