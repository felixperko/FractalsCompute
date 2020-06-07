package de.felixperko.fractals;

import java.util.List;

//import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aparapi.device.Device;
import com.aparapi.device.OpenCLDevice;
import com.aparapi.internal.kernel.KernelManager;
import com.aparapi.internal.kernel.KernelPreferences;
import com.aparapi.internal.opencl.OpenCLPlatform;

//import com.aparapi.device.Device;
//import com.aparapi.device.OpenCLDevice;
//import com.aparapi.internal.kernel.KernelManager;
//import com.aparapi.internal.kernel.KernelPreferences;
//import com.aparapi.internal.opencl.OpenCLPlatform;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.util.StatusPrinter;
import de.felixperko.fractals.manager.server.ServerManagers;
import de.felixperko.fractals.network.infra.connection.ClientLocalConnection;
import de.felixperko.fractals.network.interfaces.Messageable;
import de.felixperko.fractals.network.interfaces.ServerLocalMessageable;

public class FractalsMain {
	
	private static final Logger LOG = LoggerFactory.getLogger(FractalsMain.class);
	
	static ServerManagers managers;
	
//	public static int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
	public static int THREAD_COUNT = 4;
	
	public static void main(String[] args) {
		
	      List<OpenCLPlatform> platforms = (new OpenCLPlatform()).getOpenCLPlatforms();
	      System.out.println("Machine contains " + platforms.size() + " OpenCL platforms");
	      int platformc = 0;
	      for (OpenCLPlatform platform : platforms) {
	         System.out.println("Platform " + platformc + "{");
	         System.out.println("   Name    : \"" + platform.getName() + "\"");
	         System.out.println("   Vendor  : \"" + platform.getVendor() + "\"");
	         System.out.println("   Version : \"" + platform.getVersion() + "\"");
	         List<OpenCLDevice> devices = platform.getOpenCLDevices();
	         System.out.println("   Platform contains " + devices.size() + " OpenCL devices");
	         int devicec = 0;
	         for (OpenCLDevice device : devices) {
	            System.out.println("   Device " + devicec + "{");
	            System.out.println("       Type                  : " + device.getType());
	            System.out.println("       GlobalMemSize         : " + device.getGlobalMemSize());
	            System.out.println("       LocalMemSize          : " + device.getLocalMemSize());
	            System.out.println("       MaxComputeUnits       : " + device.getMaxComputeUnits());
	            System.out.println("       MaxWorkGroupSizes     : " + device.getMaxWorkGroupSize());
	            System.out.println("       MaxWorkItemDimensions : " + device.getMaxWorkItemDimensions());
	            System.out.println("   }");
	            devicec++;
	         }
	         System.out.println("}");
	         platformc++;
	      }

	      KernelPreferences preferences = KernelManager.instance().getDefaultPreferences();
	      System.out.println("\nDevices in preferred order:\n");

	      for (Device device : preferences.getPreferredDevices(null)) {
	         System.out.println(device);
	         System.out.println();
	      }
		
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		StatusPrinter.print(lc);
		
		LOG.info("user.dir: "+System.getProperty("user.dir"));
		managers = new ServerManagers();

		managers.getSystemManager().insertAvailableSystems();
		
		managers.getThreadManager().startWorkerThreads(THREAD_COUNT, false);
		managers.getServerNetworkManager().startServerConnectThread();
		
		managers.getThreadManager().startInputScannerThread();
	}
	
	public static ServerLocalMessageable registerLocalClient(Messageable clientMessageable) {
		ClientLocalConnection clientConnection = managers.getNetworkManager().createNewLocalClient(clientMessageable);
		clientMessageable.setConnection(clientConnection);
		ServerLocalMessageable slm = new ServerLocalMessageable(managers, "IN_LOCAL");
		slm.setClientLocalConnection(clientConnection);
		return slm;
	}
}
