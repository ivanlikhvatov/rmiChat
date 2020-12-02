package client;

import entity.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import java.util.*;
import java.util.List;

public class ClientGUI extends JFrame implements ActionListener{
    private ChatClientImpl chatClient;
    private String name;
    private char[] pass;
    private String gender;
    private String login;
    private User addressee;
    private List<PrivateMessage> privateMessages;
    private List<String> generalMessages;

    private Container container = this.getContentPane();
    private JList<User> activeUsers;
    private JList<DialogLastMessage> privateDialogs;
    private JPanel inputPanel;
    private JTextField nameInput, messageInput;
    private JPasswordField passInput;
    private JRadioButton radioMale, radioFemale;
    private JButton logoutButton, getRegistrationPanelButton,
            registrationButton, sendGMButton, sendPMessageButton, getStartPanelButton, getGMPanelButton,
            getDialogsPanelButton, openPrivateChatButton, getPersonalDataPanelButton, savePersonalDataChangesButton;
    private JPanel activeUsersScrollPanel;
    private JPanel pmDialogsScrollPanel;
    private JPanel generalMessagePanel;
    private JPanel dialogsPanel;
    private JTextArea generalTextArea, privateTextArea;

    public ClientGUI(){
        super("Простой чат");
        this.setBounds(400,200,550,400);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container container = this.getContentPane();

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                if(chatClient != null){
                    chatClient.disconnect();
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
                generateErrorMessage("Заполните все поля!", "Не все поля заполнены");
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

        if (e.getSource() == getPersonalDataPanelButton){
            container.removeAll();
            container.setLayout(new BorderLayout());
            container.add(getPersonalDataPanel(), BorderLayout.CENTER);
            container.revalidate();
        }

        if (e.getSource() == savePersonalDataChangesButton){
            if (nameInput.getText().isEmpty() || passInput.getPassword().length == 0 || (!radioFemale.isSelected() && !radioMale.isSelected())){
                generateErrorMessage("Заполните все поля!", "Не все поля заполнены");
                return;
            }

            String newGender = "";

            if (radioMale.isSelected()){
                newGender = radioMale.getName();
            } else if (radioFemale.isSelected()){
                newGender = radioFemale.getName();
            }

            if (name.equals(nameInput.getText()) && Arrays.equals(passInput.getPassword(), pass) && newGender.equals(gender)){
                return;
            }

            name = nameInput.getText();
            pass = passInput.getPassword();
            gender = newGender;
            chatClient.changePersonalData(name, gender, pass);
        }

        if (e.getSource() == logoutButton){
            if(chatClient != null){
                chatClient.disconnect();
                login = null;
                pass = null;
                chatClient = null;
                container.removeAll();
                container.setLayout(new BorderLayout());
                container.add(getStartPanel(), BorderLayout.CENTER);
                container.revalidate();
                this.setBounds(400,200,550,400);
            }
        }

        if (e.getSource() == sendGMButton){
            chatClient.sendGeneralMessage(messageInput.getText(), login);
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

                for (User user : activeUsers.getSelectedValuesList()){
                    loginList.add(user.getLogin());
                }
                chatClient.sendPrivateMessage(loginList, messageInput.getText());
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
            chatClient.sendPrivateMessage(addressee.getLogin(), messageInput.getText());
        }
    }

    public JPanel getStartPanel(){
        JPanel gridPanel = new JPanel(new GridLayout(1, 2, 5, 0));

        getRegistrationPanelButton = new JButton("Зарегистрироваться ");
        getRegistrationPanelButton.addActionListener(this);
        gridPanel.add(getRegistrationPanelButton);

        JPanel startPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 70));
        startPanel.add(gridPanel);

        return startPanel;
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

    public JPanel getPersonalDataPanel(){
        JPanel personalPanel = new JPanel(new BorderLayout());
        JPanel springLayout = new JPanel(new SpringLayout());
        SpringLayout layout = new SpringLayout();
        springLayout.setLayout(layout);

        JLabel loginLabel = new JLabel("Логин: ");
        JTextArea loginText = new JTextArea(login, 1, 20);
        loginText.setBorder(BorderFactory.createEtchedBorder(1));
        loginText.setEditable(false);
        JLabel nameLabel = new JLabel("Имя: ");
        nameInput = new JTextField(name, 20);
        JLabel passLabel = new JLabel("Пароль: ");
        passInput = new JPasswordField(Arrays.toString(pass).replaceAll("[, \\[\\]]",""), 20);
        JLabel genderLabel = new JLabel("Укажите пол: ");
        radioMale = new JRadioButton("муж");
        radioMale.setName("male");
        radioFemale = new JRadioButton("жен");
        radioFemale.setName("female");
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(radioMale);
        buttonGroup.add(radioFemale);

        if (gender.equals("male")){
            radioMale.setSelected(true);
        }

        if (gender.equals("female")){
            radioFemale.setSelected(true);
        }

        getGMPanelButton = new JButton("общие сообщения");
        getGMPanelButton.addActionListener(this);
        getDialogsPanelButton = new JButton("личные сообщения");
        getDialogsPanelButton.addActionListener(this);
        logoutButton = new JButton("выйти");
        logoutButton.addActionListener(this);
        getPersonalDataPanelButton = new JButton("личный кабинет");
        getPersonalDataPanelButton.addActionListener(this);
        savePersonalDataChangesButton = new JButton("сохранить изменения");
        savePersonalDataChangesButton.addActionListener(this);

        JPanel chooseTypeMessagePanel = new JPanel(new GridLayout(1, 2, 28, 0));
        chooseTypeMessagePanel.add(getGMPanelButton);
        chooseTypeMessagePanel.add(getDialogsPanelButton);
        JPanel flowLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
        flowLeft.add(chooseTypeMessagePanel);
        flowLeft.setBorder(new EmptyBorder(5, 0, -5, 0));

        JPanel logoutPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        logoutPanel.add(getPersonalDataPanelButton);
        logoutPanel.add(logoutButton);
        JPanel flowRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        flowRight.add(logoutPanel);
        flowRight.setBorder(new EmptyBorder(5, 0, -5, 0));

        JPanel flowLeftAndRight = new JPanel(new GridLayout(1, 2, 5, 0));
        flowLeftAndRight.add(flowLeft);
        flowLeftAndRight.add(flowRight);
        personalPanel.add(flowLeftAndRight, BorderLayout.NORTH);

        layout.putConstraint(SpringLayout.WEST , loginLabel, 10, SpringLayout.WEST , springLayout);
        layout.putConstraint(SpringLayout.NORTH, loginLabel, 28, SpringLayout.NORTH, springLayout);
        layout.putConstraint(SpringLayout.NORTH, loginText, 25, SpringLayout.NORTH, springLayout);
        layout.putConstraint(SpringLayout.WEST , loginText, 53, SpringLayout.EAST , loginLabel);

        layout.putConstraint(SpringLayout.WEST , nameLabel, 10, SpringLayout.WEST , springLayout);
        layout.putConstraint(SpringLayout.NORTH, nameLabel, 56, SpringLayout.NORTH, springLayout);
        layout.putConstraint(SpringLayout.NORTH, nameInput, 53, SpringLayout.NORTH, springLayout);
        layout.putConstraint(SpringLayout.WEST , nameInput, 62, SpringLayout.EAST , nameLabel);

        layout.putConstraint(SpringLayout.WEST , passLabel, 10, SpringLayout.WEST , springLayout);
        layout.putConstraint(SpringLayout.NORTH, passLabel, 84, SpringLayout.NORTH, springLayout);
        layout.putConstraint(SpringLayout.NORTH, passInput, 78, SpringLayout.NORTH, springLayout);
        layout.putConstraint(SpringLayout.WEST , passInput, 40, SpringLayout.EAST , passLabel);

        layout.putConstraint(SpringLayout.WEST , genderLabel, 10, SpringLayout.WEST , springLayout);
        layout.putConstraint(SpringLayout.NORTH, genderLabel, 118, SpringLayout.NORTH, springLayout);
        layout.putConstraint(SpringLayout.NORTH, radioMale, 116, SpringLayout.NORTH, springLayout);
        layout.putConstraint(SpringLayout.WEST , radioMale, 30, SpringLayout.EAST , genderLabel);
        layout.putConstraint(SpringLayout.NORTH, radioFemale, 116, SpringLayout.NORTH, springLayout);
        layout.putConstraint(SpringLayout.WEST , radioFemale, 90, SpringLayout.EAST , genderLabel);

        layout.putConstraint(SpringLayout.NORTH, savePersonalDataChangesButton, 160, SpringLayout.NORTH, springLayout);

        springLayout.add(savePersonalDataChangesButton);
        springLayout.add(nameLabel);
        springLayout.add(nameInput);
        springLayout.add(passLabel);
        springLayout.add(passInput);
        springLayout.add(genderLabel);
        springLayout.add(radioMale);
        springLayout.add(radioFemale);
        springLayout.add(loginLabel);
        springLayout.add(loginText);
        personalPanel.add(springLayout, BorderLayout.CENTER);

        return personalPanel;
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
        getPersonalDataPanelButton = new JButton("личный кабинет");
        getPersonalDataPanelButton.addActionListener(this);

        JPanel chooseTypeMessagePanel = new JPanel(new GridLayout(1, 2, 28, 0));
        chooseTypeMessagePanel.add(getGMPanelButton);
        chooseTypeMessagePanel.add(getDialogsPanelButton);
        JPanel flowLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
        flowLeft.add(chooseTypeMessagePanel);
        flowLeft.setBorder(new EmptyBorder(5, 0, -5, 0));

        JPanel logoutPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        logoutPanel.add(getPersonalDataPanelButton);
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
        getPersonalDataPanelButton = new JButton("личный кабинет");
        getPersonalDataPanelButton.addActionListener(this);

        JPanel chooseTypeMessagePanel = new JPanel(new GridLayout(1, 2, 28, 0));
        chooseTypeMessagePanel.add(getGMPanelButton);
        chooseTypeMessagePanel.add(getDialogsPanelButton);
        JPanel flowLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
        flowLeft.add(chooseTypeMessagePanel);
        flowLeft.setBorder(new EmptyBorder(5, 0, -5, 0));

        JPanel logoutPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        logoutPanel.add(getPersonalDataPanelButton);
        logoutPanel.add(logoutButton);
        JPanel flowRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        flowRight.add(logoutPanel);
        flowRight.setBorder(new EmptyBorder(5, 0, -5, 0));

        JPanel flowLeftAndRight = new JPanel(new GridLayout(1, 2, 5, 0));
        flowLeftAndRight.add(flowLeft);
        flowLeftAndRight.add(flowRight);

        if (privateDialogs.getModel().getSize() == 0){
            setPrivateDialogsPanel(new String[]{"nothing", "У вас нет сообщений", "", ""});
        }

        dialogsPanel.add(flowLeftAndRight, BorderLayout.NORTH);
        dialogsPanel.add(activeUsersScrollPanel, BorderLayout.WEST);
        dialogsPanel.add(pmDialogsScrollPanel, BorderLayout.CENTER);
        activeUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        activeUsers.removeSelectionInterval(0, activeUsers.getModel().getSize() - 1);
        privateDialogs.removeSelectionInterval(0, privateDialogs.getModel().getSize() - 1);

        return dialogsPanel;
    }

    public JPanel getPrivateMessagePanel(){
        JPanel privateMessagePanel = new JPanel(new BorderLayout());

        sendPMessageButton = new JButton("отправить");
        sendPMessageButton.setMargin(new Insets(5, 10, 5, 10));
        sendPMessageButton.addActionListener(this);
        getGMPanelButton = new JButton("общие сообщения");
        getGMPanelButton.addActionListener(this);
        getDialogsPanelButton = new JButton("личные сообщения");
        getDialogsPanelButton.addActionListener(this);
        logoutButton = new JButton("выйти");
        logoutButton.addActionListener(this);
        getPersonalDataPanelButton = new JButton("личный кабинет");
        getPersonalDataPanelButton.addActionListener(this);

        JPanel chooseTypeMessagePanel = new JPanel(new GridLayout(1, 2, 28, 0));
        chooseTypeMessagePanel.add(getGMPanelButton);
        chooseTypeMessagePanel.add(getDialogsPanelButton);
        JPanel flowLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
        flowLeft.add(chooseTypeMessagePanel);
        flowLeft.setBorder(new EmptyBorder(5, 0, -5, 0));

        JPanel logoutPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        logoutPanel.add(getPersonalDataPanelButton);
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

    public JPanel getGeneralTextPanel(){
        String welcome = "[server] : " + "Приветствуем вас в нашем чате!\n";
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

    public void setPrivateDialogsPanel(String[] messageDetails){
        pmDialogsScrollPanel = new JPanel(new BorderLayout());
        DefaultListModel<DialogLastMessage> tempPrivateMessages = new DefaultListModel<>();
        List<DialogLastMessage> dialogLastMessagesList = new ArrayList<>();

        String login = messageDetails[0];
        String username = messageDetails[1];
        String gender = messageDetails[2];
        String message = messageDetails[3];
        User user = new User(login, username, gender);

        if (privateDialogs != null){

            for (int i = 0; i < privateDialogs.getModel().getSize(); i++){
                dialogLastMessagesList.add(privateDialogs.getModel().getElementAt(i));
            }

            dialogLastMessagesList
                    .removeIf(dialogLastMessage -> dialogLastMessage.getInterlocutor().getLogin().equals(login)
                                    || dialogLastMessage.getInterlocutor().getLogin().equals("nothing")
                    );


        }

        dialogLastMessagesList.add(0, new DialogLastMessage(user, message));
        tempPrivateMessages.addAll(dialogLastMessagesList);
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

    public void setActiveUsersPanel(List<String[]> users){
        activeUsersScrollPanel = new JPanel(new BorderLayout());
        DefaultListModel<User> tempUsersList = new DefaultListModel<>();

        for (String[] userDetails : users){
            tempUsersList.addElement(new User(userDetails[0], userDetails[1], userDetails[2]));
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
        userLabel.setFont(new Font("Meiryo", Font.PLAIN, 16));

        activeUsersScrollPanel.add(userLabel, BorderLayout.NORTH);
        activeUsersScrollPanel.add(openPrivateChatButton, BorderLayout.SOUTH);
        activeUsersScrollPanel.add(listScrollPane, BorderLayout.CENTER);
        activeUsersScrollPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 30, 0));

        if (generalMessagePanel != null){
            generalMessagePanel.add(activeUsersScrollPanel, BorderLayout.WEST);
        }

        if (dialogsPanel != null){
            dialogsPanel.add(activeUsersScrollPanel, BorderLayout.WEST);
        }
    }

    public void updateUserListPanel(List<String[]> activeUsers) {
        if (generalMessagePanel != null){
            generalMessagePanel.remove(activeUsersScrollPanel);
        }

        if (dialogsPanel != null){
            dialogsPanel.remove(activeUsersScrollPanel);
        }

        setActiveUsersPanel(activeUsers);
        activeUsersScrollPanel.repaint();
        activeUsersScrollPanel.revalidate();
    }

    public void updateGeneralMessages(String message) {
        generalMessages.add(message);

        if (generalTextArea != null) {
            generalTextArea.append(message);
            generalTextArea.setCaretPosition(generalTextArea.getDocument().getLength());
        }

        if (messageInput != null){
            messageInput.setText("");
        }
    }

    public void updatePrivateMessages(Map<String, String> messageDetails, String[] interlocutorAndLastMessage) {
        PrivateMessage pm = new PrivateMessage();
        pm.setAddressee(new User(messageDetails.get("addresseeLogin"), messageDetails.get("addresseeName"), messageDetails.get("addresseeGender")));
        pm.setSender(new User(messageDetails.get("authorLogin"), messageDetails.get("authorName"), messageDetails.get("authorGender")));
        pm.setText(messageDetails.get("message"));

        if (dialogsPanel != null){
            dialogsPanel.remove(pmDialogsScrollPanel);
        }

        setPrivateDialogsPanel(interlocutorAndLastMessage);
        pmDialogsScrollPanel.repaint();
        pmDialogsScrollPanel.revalidate();
        privateMessages.add(pm);

        if (messageDetails.get("authorLogin").equals(this.login)){
            if (messageInput != null){
                messageInput.setText("");
            }
        }

        if (privateTextArea != null){
            privateTextArea.append(messageDetails.get("message"));
            privateTextArea.setCaretPosition(privateTextArea.getDocument().getLength());
        }
    }

    public void generateErrorMessage(String text, String title) {
        JOptionPane.showMessageDialog(null, text, title, JOptionPane.ERROR_MESSAGE);
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

    public void setLogin(String login){
        this.login = login;
    }

    public void assignPrivateMessages() {
        this.privateMessages = new ArrayList<>();
    }

    public void assignGeneralMessages() {
        this.generalMessages = new ArrayList<>();
    }

    public void assignPrivateDialogs() {
        this.privateDialogs = new JList<>();
    }

    public static void main(String[] args){
        ClientGUI app = new ClientGUI();
        app.setVisible(true);
    }
}
