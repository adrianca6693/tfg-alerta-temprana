const TelegramBot = require('node-telegram-bot-api');
const { Pool } = require('pg');
const telegram_token = process.env.TELEGRAM_TOKEN;
const bot = new TelegramBot(telegram_token, { polling: true });
const pool = new Pool({
  host: process.env.DB_HOST,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME,
  port: 5432,
});
bot.onText(/\/start (\d+)/, async (msg, match) => {
    console.log(" Vinculación desde Telegram:", msg.chat.first_name, "ID de contacto:", match[1]);
    const chatId = msg.chat.id;           
    const telegramName = msg.chat.first_name; 
    const contactId = match[1];          

    try {
       
        const result = await pool.query('UPDATE contactos SET chat_id = $1 WHERE contactid = $2 RETURNING name',[chatId, contactId]);

        if (result.rowCount > 0) {
            const userName = result.rows[0].name;
            
            
            bot.sendMessage(
                chatId, 
                `Te has vinculado con éxito como contacto de emergencia, **${userName}**.`
            );
            console.log(` Contacto vinculado con ID ${contactId} de (${userName})`);
        } else {
            bot.sendMessage(chatId, "El enlace ya no es válido");
        }

    } catch (err) {
        console.error("Error al vincular en Telegram:", err.message);
        bot.sendMessage(chatId, "Hubo un error al registrarte. Inténtalo de nuevo.");
    }
});
bot.onText(/\/history$/, async (msg, match) => {
    console.log(" Historial desde Telegram:", msg.chat.first_name, "ID de contacto:", match[1]);
    const chatId = msg.chat.id;
    try {
        const userid = await pool.query('SELECT userid FROM contactos WHERE chat_id = $1',[chatId]);

        if (userid.rowCount === 0) {
            bot.sendMessage(chatId, "No se encontró un usuario vinculado a este contacto.");
            return;
        }

        const tripids = await pool.query('SELECT tripid FROM trayectos WHERE userid = $1',[userid.rows[0].userid]);

        if (tripids.rowCount === 0) {
            bot.sendMessage(chatId, "No se encontraron viajes para este usuario.");
            return;
        }

        const history = await Promise.all(tripids.rows.map(async (row) => {
            const tripid = row.tripid;
            const alerts = await pool.query('SELECT description, timestamp FROM alertas WHERE tripid = $1 ORDER BY timestamp DESC',[tripid]);    
            bothAlerts = alerts.rows.map(alert => `- ${alert.description} (${new Date(alert.timestamp).toLocaleString()})`).join('\n');
            return `*Viaje ID:* ${tripid}\n${bothAlerts}`;
        }
        ));
        bot.sendMessage(chatId, `*Historial de alertas para el usuario vinculado:*\n\n${history.join('\n\n')}`, { parse_mode: 'Markdown' });
    } catch (err) {
        console.error("Error al obtener el historial en Telegram:", err.message);
        bot.sendMessage(chatId, "Hubo un error al obtener el historial. Inténtalo de nuevo.");
    }
});
async function lanzarAlerta(userId, lat, lon,message) {
    try {
        
        const result = await pool.query('SELECT chat_id, name FROM contactos WHERE userid = $1 AND chat_id IS NOT NULL',[userId]);

        if (result.rowCount === 0) {
            console.log(`El usuario ${userId} no tiene contactos de Telegram vinculados.`);
            return;
        }

       
        const googleMapsUrl = `https://www.google.com/maps?q=${lat},${lon}`;
        
        const alert = ` *¡ALERTA!* \n\n` +`El usuario puede estar en peligro:\n\n` +` *Última ubicación conocida:* [Ver en Google Maps](${googleMapsUrl})\n` + `\n*Descripción de la alerta:* ${message}` ;

        const send = result.rows.map(contacto => {
            return bot.sendMessage(contacto.chat_id, alert, { parse_mode: 'Markdown' })
                .then(() => console.log(`Mensaje de emergencia enviado a ${contacto.name}`))
                .catch(err => console.error(` Error enviando a ${contacto.name}:`, err.message));
        });

        await Promise.all(send);

    } catch (err) {
        console.error("Error en el broadcast de emergencia:", err.message);
    }
}
module.exports = { lanzarAlerta };