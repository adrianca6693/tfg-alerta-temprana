const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const TelegramBot = require('node-telegram-bot-api');
const SECRET = process.env.JWT_SECRET;
const telegram_token = process.env.TELEGRAM_TOKEN;
const bot = new TelegramBot(telegram_token, { polling: true });

const { Pool } = require('pg');

const app = express();
const port = 3000;
const axios = require('axios');
const turf = require('@turf/turf');
const polylineLib = require('@mapbox/polyline');
app.use(express.json());

const pool = new Pool({
  host: process.env.DB_HOST,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME,
  port: 5432,
});


const connectWithRetry = () => {
  pool.query('SELECT NOW()')
    .then(res => console.log('CONECTADO A POSTGRES:', res.rows.now))
    .catch(err => {
      console.error('Error de conexión, reintentando en 5 segundos...');
      setTimeout(connectWithRetry, 5000);
    });
};

connectWithRetry();


// Entorno de prueba //


app.get('/usuarios/:id', async (req, res) => {
  try {
    
    const result = await pool.query('SELECT * FROM usuarios WHERE idusuario = $1', [req.params.id]);
    
    if (result.rowCount === 0) {
      return res.status(404).json({
        mensaje: "Usuario no encontrado"
      });
    }

    res.json(result.rows[0]);
  } catch (err) {
    res.status(500).json({
      mensaje: "Error al consultar usuarios",
      error: err.message
    });
  }
});
app.post('/usuarios', async (req, res) => {
  try {
    const { nombre, email, contrasenia, telefono, pin } = req.body;
    const result = await pool.query(
      'INSERT INTO usuarios (nombre, email, contrasenia, telefono, pin) VALUES ($1, $2, $3, $4, $5) RETURNING *',
      [nombre, email, contrasenia, telefono, pin]
    );
    res.status(201).json({
      mensaje: "Usuario creado",
      usuario: result.rows[0]
    });
  } catch (err) {
    res.status(500).json({
      mensaje: "Error al crear usuario",
        error: err.message
    });
    }
});
app.put('/usuarios/:id', async (req, res) => {
  try {
    const { nombre, email, contrasenia, telefono, pin } = req.body;
    const result = await pool.query(
      'UPDATE usuarios SET nombre = $1, email = $2, contrasenia = $3, telefono = $4, pin = $5 WHERE idusuario = $6 RETURNING *',
      [nombre, email, contrasenia, telefono, pin, req.params.id]
    );
    res.json({
      mensaje: "Usuario actualizado",
      usuario: result.rows[0]
    });
  } catch (err) {
    res.status(500).json({
      mensaje: "Error al actualizar usuario",
      error: err.message
    });
  }
});


/*app.post('/contactos', async (req, res) => {
  try {
    const {userId, name, phoneNumber, priority } = req.body;
    const result = await pool.query(
      'INSERT INTO contactos ( userId, name, phoneNumber, priority) VALUES ($1, $2, $3, $4) RETURNING *',
      [ userId, name, phoneNumber, priority]
    );
    res.status(201).json({
      mensaje: "Contacto creado",
      contacto: result.rows[0]
    });
  } catch (err) {
    res.status(500).json({
      mensaje: "Error al crear contacto",
      error: err.message
    });
  }
});*/

/*app.get('/contactos/:id', async (req, res) => {
  try {
    const result = await pool.query('SELECT * FROM contactos WHERE idcontacto = $1', [req.params.id]);
    if (result.rowCount === 0) {
      return res.status(404).json({
        mensaje: "Contacto no encontrado"
      });
    }
    res.json({
        total: result.rowCount,
        contacto: result.rows[0]
    });
  } catch (err) {
    res.status(500).json({
      mensaje: "Error al consultar contacto",
        error: err.message
    });
  }
});*/

app.get('/usuarios/:id/contactos', async (req, res) => {
  try {
    const result = await pool.query('SELECT * FROM contactos WHERE usuario_id = $1', [req.params.id]);
    res.json({
      total: result.rowCount,
      contactos: result.rows
    });
  } catch (err) {
    res.status(500).json({
      mensaje: "Error al consultar contactos",
      error: err.message
    });
  }
});
app.put('/contactos/:id', async (req, res) => {
  try {
    const { nombre, telefono, prioridad } = req.body;
    const result = await pool.query(
        'UPDATE contactos SET nombre = $1, telefono = $2, prioridad = $3 WHERE idcontacto = $4 RETURNING *',
        [nombre, telefono, prioridad, req.params.id]
    );
    res.json({
        mensaje: "Contacto actualizado",
        contacto: result.rows[0]
    });
  } catch (err) {
    res.status(500).json({
      mensaje: "Error al actualizar contacto",
        error: err.message
    });
  }
});

app.delete('/contactos/:id', async (req, res) => {
  try {
    const result = await pool.query('DELETE FROM contactos WHERE idcontacto = $1 RETURNING *', [req.params.id]);
    if (result.rowCount === 0) {
      return res.status(404).json({
        mensaje: "Contacto no encontrado"
      });
    }
    res.json({
      mensaje: "Contacto eliminado",
      contacto: result.rows[0]
    });
  } catch (err) {
    res.status(500).json({
      mensaje: "Error al eliminar contacto",
      error: err.message
    });
  }
});

app.post('/trayectos', async (req, res) => {
  try {
    const { usuario_id, nombre,inilat, inilon, destlat, destlon,ruta,estado } = req.body;
    const result = await pool.query(
      'INSERT INTO trayectos (usuario_id, nombre, inilat, inilon, destlat, destlon,ruta,estado) VALUES ($1, $2, $3, $4, $5,$6,$7,$8) RETURNING *',
      [usuario_id, nombre,inilat, inilon, destlat, destlon,ruta,estado]
    );
    res.status(201).json({
      mensaje: "Trayecto creado",
      trayecto: result.rows[0]
    });
  } catch (err) {
    res.status(500).json({
      mensaje: "Error al crear trayecto",
        error: err.message
    });
  }
});

app.get('/trayectos/:id/estado', async (req, res) => {
    try {
        const result = await pool.query('SELECT estado FROM trayectos WHERE idtrayecto = $1', [req.params.id]);
        if (result.rowCount === 0) {
            return res.status(404).json({
                mensaje: "Trayecto no encontrado"
            });
        }
        res.json({
            total: result.rowCount,
            trayectos: result.rows
        });
    } catch (err) {
        res.status(500).json({
            mensaje: "Error al consultar trayectos",
            error: err.message
        });
    }
});

app.patch('/trayectos/:trayectoId', async (req, res) => {
    try {
        const { estado } = req.body;
        const result = await pool.query(
            'UPDATE trayectos SET estado = $1 WHERE idtrayecto = $2 RETURNING *',
            [estado, req.params.trayectoId]
        );
        if (result.rowCount === 0) {
            return res.status(404).json({
                mensaje: "Trayecto no encontrado"
            });
        } else {
            res.json({
                mensaje: "Estado del trayecto actualizado",
                trayecto: result.rows[0]
            });
        }
    } catch (err) {
        res.status(500).json({
            mensaje: "Error al actualizar estado del trayecto",
            error: err.message
        });
    }
});

app.post('/trayectos/:trayectoId/posiciones', async (req, res) => {
    try {
        const { latitud, longitud } = req.body;
        const result = await pool.query(
            'INSERT INTO posiciones (trayecto_id, lat, lon) VALUES ($1, $2, $3) RETURNING *',
            [req.params.trayectoId, latitud, longitud]
        );
        res.status(201).json({
            mensaje: "Posición creada",
            posicion: result.rows[0]
        });
    } catch (err) {
        res.status(500).json({
            mensaje: "Error al crear posición",
            error: err.message
        });
    }
});

app.get('/trayectos/:trayectoId/posiciones', async (req, res) => {
    try {
        const result = await pool.query('SELECT * FROM posiciones WHERE trayecto_id = $1', [req.params.trayectoId]);
        res.json({
            total: result.rowCount,
            posiciones: result.rows
        });
    } catch (err) {
        res.status(500).json({
            mensaje: "Error al consultar posiciones",
            error: err.message
        });
    }
});

app.post('/trayectos/:trayectoId/alertas', async (req, res) => {
    try {
        const { trayecto_id, descripcion, tipo, lat, lon } = req.body;
        const result = await pool.query(
            'INSERT INTO alertas (trayecto_id, tipo, descripcion, lat, lon) VALUES ($1, $2, $3, $4, $5) RETURNING *',
            [req.params.trayectoId, tipo, descripcion, lat, lon]
        );
        res.status(201).json({
            mensaje: "Alerta creada",
            alerta: result.rows[0]
        });
    } catch (err) {
        res.status(500).json({
            mensaje: "Error al crear alerta",
            error: err.message
        });
    }
});




// Entorno de desarrollo //

function authMiddleware(req, res, next) {
    const auth = req.headers['authorization'];
    const token = auth?.split(' ')[1];
    if (!token) return res.status(401).json({ message: 'No autorizado' });

    try {
        const decoded = jwt.verify(token, SECRET);
        req.userId = decoded.userId; 
        next();
    } catch {
        res.status(403).json({ message: 'Token inválido' });
    }
}

app.post('/login', async (req, res) => {
    const { email, password } = req.body;
    try {

        
        const result = await pool.query(
            'SELECT * FROM usuarios WHERE email = $1',
            [email]
        );

        
        if (result.rowCount === 0) {
            return res.status(401).json({ mensaje: "Credenciales incorrectas" });
        }
        const user = result.rows[0];
        const isValid = await bcrypt.compare(password, user.password);
        if (isValid) {
            
            delete user.password; 
            const token = jwt.sign({ userId: user.userid },SECRET,{ expiresIn: '7d' });
            res.json({
                message: "Login correcto",
                user: user,
                token: token
            });

        } else {
            
            res.status(401).json({ mensaje: "Credenciales incorrectas" });
        }
        
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});


app.post('/register', async (req, res) => {
  try {
    const { name, email, password, phoneNumber, pin } = req.body;
    console.log("Datos recibidos en /register:");
    console.log(req.body);
    const salt = await bcrypt.genSalt(10);
    const hashedPass = await bcrypt.hash(password, salt);
    const result = await pool.query(
      'INSERT INTO usuarios (name, email, password, phoneNumber, pin) VALUES ($1, $2, $3, $4, $5) RETURNING *',
      [name, email, hashedPass, phoneNumber, pin]
    );
    res.status(201).json({
      message: "Usuario creado",
      user: result.rows[0]
    });
  } catch (err) {
    res.status(500).json({
      message: "Error al crear usuario",
        error: err.message
    });
    }
});
app.get('/contacts/:userid', authMiddleware, async (req, res) => {
  try {
    const result = await pool.query('SELECT * FROM contactos WHERE userid = $1', [req.params.userid]);
    
    res.json(result.rows);
  } catch (err) {
    res.status(500).json({
      mensaje: "Error al consultar contacto",
        error: err.message
    });
  }
});

app.post('/contacts/add', authMiddleware, async (req, res) => {
  try {
    const {userid, name, phoneNumber } = req.body;
    const result = await pool.query(
      'INSERT INTO contactos ( userid, name, phoneNumber) VALUES ($1, $2, $3) RETURNING *',
      [ userid, name, phoneNumber]
    );
    res.status(201).json({
      mensaje: "Contacto creado",
      contacto: result.rows[0]
    });
  } catch (err) {
    res.status(500).json({
      mensaje: "Error al crear contacto",
      error: err.message
    });
  }
});
app.put('/contacts/update/:id', authMiddleware, async (req, res) => {
  try {
    const { name, phonenumber } = req.body;
    const result = await pool.query(
        'UPDATE contactos SET name = $1, phonenumber = $2 WHERE contactid = $3 RETURNING *',
        [name, phonenumber, req.params.id]
    );
    res.json({
        mensaje: "Contacto actualizado",
        contacto: result.rows[0]
    });
  } catch (err) {
    res.status(500).json({
      mensaje: "Error al actualizar contacto",
        error: err.message
    });
  }
});
app.delete('/contacts/delete/:id', authMiddleware, async (req, res) => {
  try {
    const result = await pool.query('DELETE FROM contactos WHERE contactid = $1 RETURNING *', [req.params.id]);
    if (result.rowCount === 0) {
      return res.status(404).json({
        mensaje: "Contacto no encontrado"
      });
    }
    res.json({
      mensaje: "Contacto eliminado",
      contacto: result.rows[0]
    });
  } catch (err) {
    res.status(500).json({
      mensaje: "Error al eliminar contacto",
      error: err.message
    });
  }
});
app.post('/trips', authMiddleware, async (req, res) => {
  try {
    const { userid, name,inilat, inilon, destlat, destlon,route,status } = req.body;
    const result = await pool.query(
      'INSERT INTO trayectos (userid, name,inilat, inilon, destlat, destlon,route,status) VALUES ($1, $2, $3, $4, $5,$6,$7,$8) RETURNING *',
      [userid, name,inilat, inilon, destlat, destlon,route,status]
    );
    res.status(201).json({
      mensaje: "Trayecto creado",
      trayecto: result.rows[0]
    });
  } catch (err) {
    res.status(500).json({
      mensaje: "Error al crear trayecto",
        error: err.message
    });
  }
});

app.post('/trips/newtrip', authMiddleware, async (req, res) => {
  const { userid, name, inilat, inilon, destlat, destlon,status } = req.body;
  const MAPBOX_TOKEN = 'pk.eyJ1IjoiYWRyaWFuY3VhYXJjIiwiYSI6ImNtb3Y4MXJqcTA0bjEycnNjNWI4OTBkcGQifQ.eSxpAFz2fX7mSMwyn3SDQw';

  try{
    const url = `https://api.mapbox.com/directions/v5/mapbox/driving/${inilon},${inilat};${destlon},${destlat}?geometries=polyline6&overview=full&access_token=${MAPBOX_TOKEN}`;
    const response = await axios.get(url);
    const polyline = response.data.routes[0].geometry;
    const result = await pool.query(
      'INSERT INTO trayectos (userid, name,inilat, inilon, destlat, destlon,route,status) VALUES ($1, $2, $3, $4, $5,$6,$7,$8) RETURNING *',
      [userid, name,inilat, inilon, destlat, destlon,polyline,status]
    );
    res.status(201).json({
      message: "Trayecto creado",
      route: polyline,
      tripid: result.rows[0].tripid
    });
  } catch (err) {
    res.status(500).json({
      mensaje: "Error al crear trayecto",
        error: err.message
    });
  }

});
 
app.patch('/trips/finish/:id/:pin', authMiddleware, async (req, res) => {
   const { id, pin } = req.params;
   try {
     const result = await pool.query(
        'UPDATE trayectos SET status = $1 WHERE tripid = $2 AND userid = ( SELECT userid FROM usuarios WHERE userid = trayectos.userid AND pin = $3) RETURNING *',
        ['finished', id, pin]
     );
      if (result.rowCount === 0) {
        return res.status(404).json({
          message: "Trayecto no encontrado o PIN incorrecto"
        });
      }
      res.json({
        message: "Trayecto finalizado"
        
      });
   } catch (err) {
     res.status(500).json({
       message: "Error al finalizar trayecto",
       error: err.message
     });
   }



});


app.post('/trips/checkDistance', authMiddleware, async (req, res) => {
    const { tripid, currentLat, currentLon } = req.body;

    try {
        const result = await pool.query('SELECT route,destlat,destlon FROM trayectos WHERE tripid = $1', [tripid]);
        if (result.rowCount === 0) {
            return res.status(404).json({
                message: "Trayecto no encontrado"
            });
        }
        const { route: routeChain, destlat, destlon } = result.rows[0];

        await pool.query('INSERT INTO posiciones (tripid, lat, lon) VALUES ($1, $2, $3)', [tripid, currentLat, currentLon]);
        const history = await pool.query('SELECT lat, lon FROM posiciones WHERE tripid = $1 ORDER BY posid DESC LIMIT 2',[tripid]);
        //const routeChain = result.rows[0].route;
        const decodeCoords = polylineLib.decode(routeChain,6);
        const turfCoords = decodeCoords.map(p => [p[1], p[0]]);
        const route = turf.lineString(turfCoords);

        const currentPoint = turf.point([currentLon, currentLat]);
        const destinationPoint = turf.point([destlon, destlat]);
        const distanceRoute = turf.pointToLineDistance(currentPoint, route, { units: 'meters' });
        const distanceDest = turf.distance(currentPoint, destinationPoint, { units: 'meters' });

        let isValid = true;
        
          if(distanceRoute > 200) {
              
              if(history.rowCount > 1) {
                  const previous = history.rows[history.rowCount - 2];
                  const prevPoint = turf.point([previous.lon, previous.lat]);
                  const prevDestDistance = turf.distance(prevPoint, destinationPoint, { units: 'meters' });
                  if(distanceDest < prevDestDistance) {
                      isValid = true;
                  }
                  else{
                      isValid = false;
                  }
                }
                else{
                  isValid = true;
                }
          }
          else if(distanceDest < 5 ){
              await pool.query('UPDATE trayectos SET status = $1 WHERE tripid = $2',['finished', tripid]);
          }
                res.json({
                  message: "Posición registrada",
                  isValid: isValid,
                });
              
        } catch (err) {
            res.status(500).json({
                message: "Error al verificar ubicación",
                error: err.message
            });
       
         }
  });
app.post('/trips/checkPosition', authMiddleware, async (req, res) => {
    const { tripid, currentLat, currentLon } = req.body;

    try {
        await pool.query('INSERT INTO posiciones (tripid, lat, lon) VALUES ($1, $2, $3)',[tripid, currentLat, currentLon]);
        const history = await pool.query('SELECT lat, lon FROM posiciones WHERE tripid = $1 ORDER BY posid DESC LIMIT 3',[tripid]);
       
        
       
        let isStopped = false;
        let timesStopped = await pool.query('SELECT times_stopped FROM trayectos WHERE tripid = $1',[tripid]);
        let timesTurned = await pool.query('SELECT times_turned FROM trayectos WHERE tripid = $1',[tripid]);
        let isSharpTurn = false;

        

        if(history.rowCount > 1) {
          const actual = history.rows[history.rowCount - 1];
          const previous = history.rows[history.rowCount - 2];

          const p1 = turf.point([actual.lon, actual.lat]);
          const p2 = turf.point([previous.lon, previous.lat]);

          const distance = turf.distance(p1, p2, { units: 'meters' });
          if(distance < 5) {
              if(timesStopped.rows[0].times_stopped > 10) {
                //aquí habría que avisar al contacto de emergencia
                isStopped = true;
              }
            
             
              const now = new Date();
          

            await pool.query('UPDATE trayectos SET times_stopped = times_stopped + 1 WHERE tripid = $1',[tripid]);
            
          }
          else{
            
            await pool.query('UPDATE trayectos SET times_stopped = 0 WHERE tripid = $1',[tripid]);
          }
        }
        if(history.rowCount > 2) {
          const a = history.rows[2];
          const b = history.rows[1];
          const c = history.rows[0];
          
          const v1 = { x: b.lon - a.lon, y: b.lat - a.lat };
          const v2 = { x: c.lon - b.lon, y: c.lat - b.lat };

          const dot = v1.x * v2.x + v1.y * v2.y;
          const m1 = Math.sqrt(v1.x ** 2 + v1.y ** 2);
          const m2 = Math.sqrt(v2.x ** 2 + v2.y ** 2);

          if(m1 > 0.0001 && m2 > 0.0001) {
                const cos = Math.max(-1, Math.min(1, dot / (m1 * m2)));
                const degrees = Math.acos(cos) * (180 / Math.PI);

                if(degrees > 120) {
                    await pool.query('UPDATE trayectos SET times_turned = times_turned + 1 WHERE tripid = $1',[tripid]);
                    
                    if(timesTurned.rows[0].times_turned > 3) {
                      isSharpTurn = true;
                    }
                    else{
                      isSharpTurn = false;
                    }
                }
          }
        }
          res.json({
            message: "Posición registrada",
            isStopped: isStopped,
            isSharpTurn: isSharpTurn
            
        });
        
    }catch (err) {
        res.status(500).json({
            message: "Error al registrar posición",
            error: err.message
        });
    }
    
   


});


app.listen(port, () => {
  console.log(`Servidor corriendo en http://localhost:${port}`);
});