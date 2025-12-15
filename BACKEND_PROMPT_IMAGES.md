# 🔧 PROMPT POUR DÉVELOPPEUR BACKEND - Correction des URLs d'images

## 📋 Problème identifié

Les images uploadées depuis un appareil ne s'affichent pas correctement sur un autre appareil. Le problème vient probablement du format des URLs retournées par le backend.

## 🔍 Analyse du problème

Le frontend Android utilise la fonction `buildImageUrl()` qui :
1. Vérifie si l'URL est déjà complète (commence par `http://` ou `https://`)
2. Si non, construit l'URL en ajoutant le `SERVER_URL` au chemin

**Problème actuel :** Le backend peut retourner des URLs dans différents formats :
- URLs complètes avec `http://localhost:3000` (ne fonctionne pas sur d'autres appareils)
- Chemins relatifs comme `/uploads/groups/filename.jpg`
- Chemins relatifs comme `uploads/groups/filename.jpg`
- Juste le nom du fichier `filename.jpg`

## ✅ Solution recommandée

### 1. Format des URLs retournées

**Le backend DOIT retourner des URLs complètes avec l'IP/host accessible depuis tous les appareils :**

```javascript
// ❌ MAUVAIS - Ne fonctionne que sur le même appareil
imageUrl: "http://localhost:3000/uploads/groups/image.jpg"

// ❌ MAUVAIS - Ne fonctionne pas si le frontend utilise une IP différente
imageUrl: "/uploads/groups/image.jpg"

// ✅ BON - URL complète avec l'IP du serveur
imageUrl: "http://192.168.100.20:3000/uploads/groups/image.jpg"
```

### 2. Configuration du serveur

Le backend doit utiliser une variable d'environnement pour l'URL de base :

```javascript
// .env
BASE_URL=http://192.168.100.20:3000
// OU pour production
BASE_URL=https://votre-domaine.com
```

### 3. Endpoints concernés

Tous les endpoints qui retournent des images doivent utiliser cette URL de base :

#### a) Upload d'image de groupe
```javascript
// POST /groups/upload-image
// Retourne :
{
  "imageUrl": "http://192.168.100.20:3000/uploads/groups/1234567890.jpg"
}
```

#### b) Upload d'image de message
```javascript
// POST /groups/upload-message-image
// Retourne :
{
  "imageUrl": "http://192.168.100.20:3000/uploads/messages/1234567890.jpg"
}
```

#### c) Récupération de groupe
```javascript
// GET /groups/:id
// Retourne :
{
  "_id": "...",
  "name": "...",
  "image": "http://192.168.100.20:3000/uploads/groups/1234567890.jpg", // ✅ URL complète
  ...
}
```

#### d) Récupération de messages
```javascript
// GET /groups/:id/messages
// Retourne :
[
  {
    "_id": "...",
    "content": "...",
    "images": [
      "http://192.168.100.20:3000/uploads/messages/1234567890.jpg" // ✅ URL complète
    ],
    ...
  }
]
```

### 4. Exemple de code backend (Node.js/Express)

```javascript
// config/images.js
const BASE_URL = process.env.BASE_URL || 'http://localhost:3000';

function buildImageUrl(filename, type = 'groups') {
  // type peut être 'groups' ou 'messages'
  return `${BASE_URL}/uploads/${type}/${filename}`;
}

// Dans le contrôleur d'upload
app.post('/groups/upload-image', upload.single('image'), (req, res) => {
  const filename = req.file.filename;
  const imageUrl = buildImageUrl(filename, 'groups');
  
  res.json({ imageUrl });
});

// Dans le modèle Group
const groupSchema = new Schema({
  name: String,
  image: {
    type: String,
    get: function(value) {
      // Si c'est déjà une URL complète, retourner telle quelle
      if (value && (value.startsWith('http://') || value.startsWith('https://'))) {
        return value;
      }
      // Sinon, construire l'URL complète
      if (value) {
        return buildImageUrl(value, 'groups');
      }
      return null;
    }
  }
});
```

### 5. Vérifications à effectuer

- [ ] Toutes les URLs d'images retournées sont complètes (commencent par `http://` ou `https://`)
- [ ] L'IP/host dans l'URL est accessible depuis tous les appareils du réseau local
- [ ] Les chemins de fichiers sont corrects (`/uploads/groups/` ou `/uploads/messages/`)
- [ ] Les fichiers sont bien servis statiquement par Express/le serveur web
- [ ] Les CORS permettent l'accès aux images depuis l'app mobile

### 6. Test à effectuer

1. Uploader une image depuis l'appareil A
2. Vérifier l'URL retournée dans la réponse JSON
3. Ouvrir cette URL dans un navigateur depuis l'appareil B
4. L'image doit s'afficher correctement
5. Si l'image s'affiche dans le navigateur mais pas dans l'app, vérifier les logs du frontend

## 📝 Notes importantes

- **Pour le développement local :** Utiliser l'IP locale du serveur (ex: `192.168.100.20:3000`)
- **Pour la production :** Utiliser le domaine complet (ex: `https://api.travelmate.com`)
- **Ne jamais utiliser `localhost`** dans les URLs retournées au frontend mobile
- **Toujours utiliser `BASE_URL`** depuis une variable d'environnement

---

**Date :** 2025-01-15  
**Version frontend :** Android - Correction affichage images depuis différents appareils

