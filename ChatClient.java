import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import static java.lang.System.out;

public class ChatClient extends JFrame implements ActionListener {
    String uname;
    String password;
    PrintWriter pw;
    BufferedReader br;
    JTextArea taMessages;
    JTextField tfInput;
    JButton btnSend, btnExit;
    Socket client;

    public ChatClient(String uname, String password, String servername) throws Exception {
        super(uname); // set title for frame
        this.uname = uname;
        this.password = password;
        client = new Socket(servername, 5200);
        br = new BufferedReader(new InputStreamReader(client.getInputStream()));
        pw = new PrintWriter(client.getOutputStream(), true);
        pw.println(uname); // send username to server
        pw.println(password); // send password to server
        buildInterface();
        new MessagesThread().start();
        pw.println("/join " + uname); // create thread for listening for messages
    }

    public void buildInterface() {
        btnSend = new JButton("Send");
        btnExit = new JButton("Exit");
        taMessages = new JTextArea();
        taMessages.setRows(10);
        taMessages.setColumns(50);
        taMessages.setEditable(false);
        tfInput = new JTextField(50);
        JScrollPane sp = new JScrollPane(taMessages, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(sp, BorderLayout.CENTER);
        
        JPanel bp = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(tfInput, BorderLayout.CENTER);
        inputPanel.add(btnSend, BorderLayout.EAST);
        bp.add(inputPanel, BorderLayout.CENTER);
        bp.add(btnExit, BorderLayout.EAST);
        add(bp, BorderLayout.SOUTH);
        
        // Set colors
        Color primaryColor = new Color(45, 156, 219);
        Color secondaryColor = new Color(240, 240, 240);
        btnSend.setBackground(primaryColor);
        btnSend.setForeground(Color.DARK_GRAY);
        btnExit.setBackground(primaryColor);
        btnExit.setForeground(Color.DARK_GRAY);
        taMessages.setBackground(secondaryColor);
        taMessages.setFont(new Font("Arial", Font.PLAIN, 14));
        tfInput.setBackground(secondaryColor);
        tfInput.setFont(new Font("Arial", Font.PLAIN, 14));
        
        btnSend.addActionListener(this);
        btnExit.addActionListener(this);
        setSize(500, 300);
        setVisible(true);
        pack();
    }
    

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == btnExit) {
            pw.println("end"); // send end to server so that server knows about the termination
            System.exit(0);
        } else {
            // send message to server
            String message = tfInput.getText();
            pw.println(message);
            taMessages.append(uname + ": " + message + "\n");
            tfInput.setText("");
        }
    }

    public static void main(String... args) {

        // take username and password from user
        String name = JOptionPane.showInputDialog(null, "Enter your name:", "Username", JOptionPane.PLAIN_MESSAGE);
        String password = JOptionPane.showInputDialog(null, "Enter your password:", "Password",
                JOptionPane.PLAIN_MESSAGE);
        String servername = "localhost";
        try {
            new ChatClient(name, password, servername);
        } catch (Exception ex) {
            out.println("Error --> " + ex.getMessage());
        }
    }

    // inner class for Messages Thread
    class MessagesThread extends Thread {
        public void run() {
            String line;
            try {
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("/join ")) {
                        String joinedUser = line.substring(6);
                        taMessages.append(joinedUser + " joined the chat\n");
                    } else {
                        taMessages.append(line + "\n");
                    }
                } // end of while
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
