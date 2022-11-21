import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

// The azure-activedirectory-library-for-java is needed to retrieve the access token from the AD.
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCredential;

public class Main {
    public static void main(String[] args) throws Exception {
        String spn = "https://database.windows.net/";
        String stsurl = "https://login.microsoftonline.com/....."; // Replace with your STS URL.
        String clientId = "316****"; // Replace with your client ID.
        String clientSecret = "*****"; // Replace with your client secret.

        String scope = spn + "/.default";
        Set<String> scopes = new HashSet<>();
        scopes.add(scope);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        IClientCredential credential = ClientCredentialFactory.createFromSecret(clientSecret);
        ConfidentialClientApplication clientApplication = ConfidentialClientApplication
                .builder(clientId, credential).executorService(executorService).authority(stsurl).build();
        CompletableFuture<IAuthenticationResult> future = clientApplication
                .acquireToken(ClientCredentialParameters.builder(scopes).build());

        IAuthenticationResult authenticationResult = future.get();
        String accessToken = authenticationResult.accessToken();
        System.out.println("Access Token: " + accessToken);
        if (false) {
            SQLServerDataSource ds = new SQLServerDataSource();
            ds.setServerName("...."); //replace with your server name
            ds.setDatabaseName("....."); //replace with your database name
            ds.setAccessToken(accessToken);
            try (Connection connection = ds.getConnection()) {
                System.out.println(connection + " success");
                try (Statement stmt1 = connection.createStatement()) {
                    stmt1.executeUpdate("insert into test1 values(5,'abc')");
                    try (ResultSet rs1 = stmt1.executeQuery("select*from test1")) {
                        while (rs1.next()) {
                            System.out.println(rs1.getInt(1) + " " + rs1.getString(2));
                        }
                    }
                }
            }
        } else {
            //passing access token in act1 from other grant_types
            String act1 = "..";
            System.out.println("trying jdbc with access token");
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Properties p = new Properties();
            p.put("databaseName","accesstokentest");
            p.put("accessToken",accessToken);
            Connection c=DriverManager.getConnection("......",p);
            System.out.println("success");
            System.out.println(c.getMetaData().getDatabaseProductName());
            try (Statement stmt1 = c.createStatement()) {
                try (ResultSet rs1 = stmt1.executeQuery("SELECT SUSER_NAME()")) {
                    while (rs1.next()) {
                        System.out.println("You have successfully logged on as: " + rs1.getString(1));
                    }
                }}
            c.close();
        }
    }
}




