# 🔧 PROMPT POUR DÉVELOPPEUR BACKEND - Suppression des notifications

## 📋 Nouvelle fonctionnalité requise

Le frontend Android nécessite des endpoints pour supprimer les notifications.

## 🔍 Endpoints requis

### 1. Supprimer une notification

**Endpoint :** `DELETE /api/notifications/:id`

**Description :** Supprime une notification spécifique par son ID.

**Headers :**
```
Authorization: Bearer {token}
```

**Réponse :**
- **200 OK** : Notification supprimée avec succès
- **404 Not Found** : Notification introuvable
- **401 Unauthorized** : Token invalide

**Exemple de réponse :**
```json
// Pas de body, juste le code de statut
```

---

### 2. Supprimer toutes les notifications lues

**Endpoint :** `DELETE /api/notifications/read-all`

**Description :** Supprime toutes les notifications marquées comme lues (`read: true`).

**Headers :**
```
Authorization: Bearer {token}
```

**Réponse :**
- **200 OK** : Toutes les notifications lues ont été supprimées
- **401 Unauthorized** : Token invalide

**Exemple de réponse :**
```json
{
  "deletedCount": 5
}
```

---

## 📝 Notes importantes

1. **Sécurité :** Vérifier que l'utilisateur ne peut supprimer que ses propres notifications
2. **Soft Delete vs Hard Delete :** 
   - Si vous utilisez un soft delete, marquer simplement `deleted: true`
   - Si vous utilisez un hard delete, supprimer définitivement de la base de données
3. **Notifications après acceptation/rejet :** 
   - Le frontend supprime automatiquement la notification après acceptation/rejet d'une demande de groupe
   - Assurez-vous que l'endpoint fonctionne correctement
4. **Comportement actuel du frontend :** 
   - Le frontend supprime les notifications localement même si les endpoints backend ne sont pas encore implémentés
   - Cela permet une meilleure UX, mais les notifications réapparaîtront au prochain rafraîchissement si elles ne sont pas supprimées côté backend
   - **IMPORTANT :** Implémenter ces endpoints pour que les suppressions soient permanentes

---

**Date :** 2025-01-15  
**Version frontend :** Android - Suppression des notifications

