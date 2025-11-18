# Instruções de Deploy no Azure

## Passo 1: Compilar o projeto
```bash
.\mvnw clean package -DskipTests
```

## Passo 2: Fazer upload do JAR para Azure

### Opção A: Via Azure Portal (Recomendado)
1. Acesse: https://portal.azure.com
2. Procure por "ouvidoria-senai" (seu App Service)
3. Vá em **Deployment Center** ou **Advanced Tools (Kudu)**
4. Faça upload do arquivo: `target/Senai-0.0.1-SNAPSHOT.jar`
5. Reinicie o App Service

### Opção B: Via FTP
1. Obtenha as credenciais FTP no Azure Portal
2. Conecte via FTP e faça upload para `/site/wwwroot/`
3. Reinicie o App Service

### Opção C: Via Git (se configurado)
```bash
git add .
git commit -m "Debug logs added for area field investigation"
git push azure main
```

## Passo 3: Verificar os logs

Após o deploy, acesse os logs em:
- Azure Portal → App Service → Log Stream
- Ou via Kudu: https://ouvidoria-senai-e9brd8b7gbg2a3f6.scm.brazilsouth-01.azurewebsites.net/api/logs/docker

## Logs esperados

Quando você enviar uma denúncia, você deve ver:

```
╔════════════════════════════════════════╗
║  DENUNCIA CONTROLLER - RECEBIDO        ║
╚════════════════════════════════════════╝
DTO.area = [ADS_REDES]
DTO.local = [...]
```

E depois:

```
╔════════════════════════════════════════╗
║  DENUNCIA SERVICE INICIADO - RECEBIDO  ║
╚════════════════════════════════════════╝
DTO.area RECEBIDO = [ADS_REDES]
>>> INICIANDO CONVERSÃO DE AREA <<<
areaStr = [ADS_REDES]
>>> TENTANDO CONVERTER: ADS_REDES
✓✓✓ SUCESSO! Área convertida: ADS_REDES
```
