package de.felixperko.fractals.network;

import java.util.ArrayList;
import java.util.List;

public class ClientConfiguration {
	int grantThreads;
	List<SystemInstanceClientData> instances = new ArrayList<>();
	transient ClientConnection connectionToClient;
}
