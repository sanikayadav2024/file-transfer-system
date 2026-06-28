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
  👉 http://localhost:8080  
- Scan the QR code or use the IP address on another device  
- Upload and download files easily  

---

## ❗ Requirements

- Java 17+  
- Maven  
- Devices connected to the same network  

---

## 🔮 Future Improvements

- 🔒 Secure file transfer (encryption)  
- 📊 Transfer history tracking  
- 👥 Multi-user support  
- 🎨 Improved UI/UX  

---

## 🤝 Contributing

Contributions are welcome!  
Feel free to fork this repository and submit a pull request 🚀  

---

## 👨‍💻 Author

**Sanika Yadav**  
GitHub: https://github.com/sanikayadav2024  

---
