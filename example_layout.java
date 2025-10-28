// Example: Creating card layout with nested panels
// In your createSystemInfoTab() method:

public static JPanel createSystemInfoTab(Computer computer) {
    JPanel mainPanel = new JPanel(new java.awt.BorderLayout(10, 10));
    
    // TOP ROW: 3 equal cards
    JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    
    JPanel cpuCard = createCard("CPU", computer);
    JPanel memCard = createCard("Memory", computer);
    JPanel diskCard = createCard("Disk", computer);
    
    cpuCard.setPreferredSize(new java.awt.Dimension(200, 150));
    memCard.setPreferredSize(new java.awt.Dimension(200, 150));
    diskCard.setPreferredSize(new java.awt.Dimension(200, 150));
    
    topRow.add(cpuCard);
    topRow.add(memCard);
    topRow.add(diskCard);
    
    // BOTTOM ROW: 2 equal cards (wider than top)
    JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    
    JPanel pciCard = createPCICard();
    JPanel usbCard = createUSBCard();
    
    pciCard.setPreferredSize(new java.awt.Dimension(300, 150));
    usbCard.setPreferredSize(new java.awt.Dimension(300, 150));
    
    bottomRow.add(pciCard);
    bottomRow.add(usbCard);
    
    // Add rows to main panel
    JPanel verticalPanel = new JPanel();
    verticalPanel.setLayout(new BoxLayout(verticalPanel, BoxLayout.Y_AXIS));
    verticalPanel.add(topRow);
    verticalPanel.add(bottomRow);
    
    return verticalPanel;
}

private static JPanel createCard(String title, Computer computer) {
    JPanel card = new JPanel(new java.awt.BorderLayout());
    card.setBorder(BorderFactory.createEtchedBorder());
    
    JLabel label = new JLabel(title, JLabel.CENTER);
    card.add(label, java.awt.BorderLayout.NORTH);
    
    JTextArea text = new JTextArea();
    text.setEditable(false);
    card.add(text, java.awt.BorderLayout.CENTER);
    
    return card;
}
