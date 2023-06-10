package ar.utn.dds.copiame;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class CopiameBot extends TelegramLongPollingBot {

	private String apiEndpoint;

	public CopiameBot(String telegramToken, String apiEndpoint) {
		super(telegramToken);
		this.apiEndpoint = apiEndpoint;
	}

	@Override
	public void onUpdateReceived(Update update) {
		Message message = update.getMessage();
		if (message.hasDocument()) {
			Document document = message.getDocument();
			if (document.getMimeType().equals("application/zip")) {
				try {
					// Obtiene el archivo
					GetFile getFile = new GetFile();
					getFile.setFileId(message.getDocument().getFileId());
					org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);
					java.io.File downloadedFile = downloadFile(file);

					// Envia el archivo a la API
					String rta = enviarLote(downloadedFile);

					// Envia el mensaje al usuario
					SendMessage responseMsg = new SendMessage();
					responseMsg.setChatId(message.getChatId());
					responseMsg.setText(rta);
					execute(responseMsg);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private String enviarLote(java.io.File downloadedFile) throws IOException, ClientProtocolException {
		HttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(this.apiEndpoint + "/analisis");

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addBinaryBody("file", downloadedFile, ContentType.DEFAULT_BINARY, "data.zip");

		HttpEntity multipart = builder.build();
		httpPost.setEntity(multipart);

		HttpResponse execute = httpClient.execute(httpPost);
		String rta = IOUtils.toString(execute.getEntity().getContent(), StandardCharsets.UTF_8.name());
		return rta;
	}

	@Override
	public String getBotUsername() {
		// Se devuelve el nombre que dimos al bot al crearlo con el BotFather
		return System.getenv("NOMBRE_BOT");
	}

	public static void main(String[] args) throws TelegramApiException {

		// Se crea un nuevo Bot API
		final TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
		String apiEndpoint = System.getenv("COPIAME_API");
		if (apiEndpoint == null) {
			apiEndpoint = "http://localhost:8080";
		}
		try {
			// Se registra el bot
			telegramBotsApi.registerBot(new CopiameBot(System.getenv("TOKEN_BOT"), apiEndpoint));
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

}
