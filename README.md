# 📁 File Transfer System using QR

A simple and efficient **File Transfer System** that allows users to transfer files between devices over a local network using QR codes. This project demonstrates real-time file sharing using a client-server architecture built with Spring Boot.

---

## 🚀 Overview

This project enables seamless file transfer between systems **without requiring cloud services, logins, or hardware connections**.

### 🔑 Key Features

- 📱 Easy access using QR code for each system  
- ⚡ Fast data transmission without internet dependency  
- 🎯 Simple and user-friendly interface  
- 🌐 Works over local network communication  

---

## 🏗️ Tech Stack

- **Backend:** Spring Boot  
- **Language:** Java  
- **Frontend:** HTML, CSS, JavaScript  
- **Build Tool:** Maven  
- **Other:** QR Code Generator, REST APIs  

---

## 📂 Project Structure

```
file-transfer-system/
│── src/
│   └── main/
│       ├── java/
│       │   └── com/example/filetransfer/
│       │       │── FileTransferApplication.java
│       │       │
│       │       └── controller/
│       │           │── FileController.java
│       │           │── QRCodeGenerator.java
│       │
│       └── resources/
│           │── application.properties
│           │
│           └── static/
│               │── index.html
```

---

## ⚙️ How It Works

1. The application runs as a Spring Boot server  
2. A QR code or IP address is generated for connection  
3. The receiving device connects using the link  
4. The sender uploads a file  
5. The receiver downloads the file instantly  

---

## 🛠️ Installation & Setup

### 1️⃣ Clone Repository
```bash
git clone https://github.com/sanikayadav2024/file-transfer-system.git
cd file-transfer-system
```

### 2️⃣ Build Project
```bash
mvn clean install
```

### 3️⃣ Run Application
```bash
mvn spring-boot:run
```

### OR run using JAR
```bash
java -jar target/file-transfer-system.jar
```

---

## 🌐 Usage

- Start the application  
- Open your browser:  
  👉 **https://localhost:8443**  
- Scan the QR code or use the IP address on another device  
- Upload files securely with auto-generated token auth  

---

## 🔒 Security (HTTPS & Token Auth)

This application includes transport-layer encryption (HTTPS) and endpoint authorization to protect local network file transfers.

### 🛡️ HTTPS & Self-Signed Certificates
For local network environments, a self-signed TLS/SSL certificate is generated and configured inside [keystore.p12](file:///c:/Users/Moiz/Desktop/file%20transfer%20system/file-transfer-system/src/main/resources/keystore.p12).
* **Browser Warnings:** When accessing the site on `https://<ip>:8443`, your browser will display a warning like *"Your connection is not private"* or *"Potential Security Risk Ahead"*. This is normal for local self-signed certificates.
* **How to proceed:** Click **Advanced** and select **Proceed to <IP Address> (unsafe)**. Your traffic will then be completely encrypted in transit.
* **Clipboard Access:** By running over HTTPS, browsers grant access to the secure clipboard API (`navigator.clipboard`), allowing the **Copy Connection Link** button to work seamlessly on mobile/remote devices.

### 🔑 Token Authentication
To prevent unauthorized users on the same WiFi network from scanning or accessing the API endpoints directly:
* A cryptographically secure random token (UUID) is generated dynamically when the application starts.
* This token is appended as a query parameter (`?token=...`) to the URL opened by the host machine and the generated QR code.
* The frontend automatically extracts this token and includes it as an `X-Auth-Token` header for upload requests.
* Unauthorized requests without a valid token are rejected with a `401 Unauthorized` status.

### ⚙️ Switching back to Plain HTTP
If you do not want to use HTTPS/TLS or handle self-signed certificate warnings, you can fall back to HTTP:
1. Open [application.properties](file:///c:/Users/Moiz/Desktop/file%20transfer%20system/file-transfer-system/src/main/resources/application.properties).
2. Comment out or delete all `server.ssl.*` lines.
3. Change `server.port` back to `8080`.
4. Restart the application.

---

## ❗ Requirements

- Java 17+  
- Maven  
- Devices connected to the same network  

---

## 🔮 Future Improvements

- 📊 Transfer history tracking  
- 👥 Multi-user support  
- 🎨 Improved UI/UX  

---
## 🤝 Contributors

Thank you to everyone who has contributed to QuickShare!

<a href="https://github.com/sanikayadav2024/file-transfer-system/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=sanikayadav2024/file-transfer-system" />
</a>

Want to contribute? Check out the issues and submit a pull request!

---

## 👨‍💻 Author

**Sanika Yadav**  
GitHub: https://github.com/sanikayadav2024  

---
