package com.lol.ml.starthackapi.service;

import com.lol.ml.starthackapi.model.Client;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class ClientService {
    private final Map<String, Client> mockClients = new HashMap<>();

    public ClientService() {
        // Mock client data
        mockClients.put("client1", new Client(
                "John Doe",
                35,
                "Software Engineer",
                "Technology, Investment, Sports",
                "Moderate",
                "10 years in tech industry, interested in long-term investments"));
        // Add more mock clients as needed
    }

    public Client getClientById(String clientId) {
        return mockClients.get(clientId);
    }
}