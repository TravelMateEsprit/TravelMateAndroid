# 🔧 Code Backend à Implémenter - Version Finale

## Dans votre fichier `insurances.service.ts`

Remplacez ou ajoutez cette méthode :

```typescript
/**
 * Récupérer les inscrits d'une assurance avec leurs détails complets
 */
async getInsuranceSubscribers(insuranceId: string, agencyId: string) {
  // Vérifier que l'assurance appartient à l'agence
  const insurance = await this.insuranceModel
    .findOne({ _id: insuranceId, agencyId: agencyId })
    .populate({
      path: 'subscribers',
      select: 'firstName lastName email phone name _id userType status', // Tous les champs nécessaires
    })
    .exec();

  if (!insurance) {
    throw new NotFoundException('Assurance non trouvée ou accès refusé');
  }

  // Retourner exactement ce format pour que l'app Android puisse le parser
  return {
    insuranceName: insurance.name,
    subscribersCount: insurance.subscribers.length,
    subscribers: insurance.subscribers, // Ce sont des objets User complets maintenant
  };
}
```

## Dans votre fichier `insurances.controller.ts`

Ajoutez ou vérifiez cet endpoint :

```typescript
import { Controller, Get, Param, UseGuards, Request, NotFoundException } from '@nestjs/common';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { RolesGuard } from '../auth/guards/roles.guard';
import { Roles } from '../auth/decorators/roles.decorator';

@Controller('insurances')
export class InsurancesController {
  constructor(private readonly insurancesService: InsurancesService) {}

  /**
   * Endpoint pour récupérer les inscrits d'une assurance
   * GET /insurances/agency/:id/subscribers
   */
  @Get('agency/:id/subscribers')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles('agence') // Seules les agences peuvent voir leurs inscrits
  async getSubscribers(
    @Param('id') insuranceId: string,
    @Request() req,
  ) {
    const agencyId = req.user.id; // ID de l'agence depuis le JWT
    return this.insurancesService.getInsuranceSubscribers(insuranceId, agencyId);
  }
}
```

## Format de Réponse Attendu

Quand l'app Android appelle `GET /insurances/agency/{id}/subscribers`, elle attend :

```json
{
  "insuranceName": "Assurance Business Travel",
  "subscribersCount": 1,
  "subscribers": [
    {
      "_id": "690a9072ea609d2f7504abe9",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "phone": "+33612345678",
      "name": "John Doe",
      "userType": "user",
      "status": "active"
    }
  ]
}
```

## ⚠️ IMPORTANT

Le champ `subscribers` doit contenir des **objets User complets**, PAS des IDs !

**Avant (Incorrect)** :
```json
{
  "subscribers": ["690a9072ea609d2f7504abe9"]  // ❌ Juste l'ID
}
```

**Après (Correct)** :
```json
{
  "subscribers": [
    {
      "_id": "690a9072ea609d2f7504abe9",
      "firstName": "John",
      "email": "john.doe@example.com",
      // ... autres champs
    }
  ]  // ✅ Objet complet
}
```

## Test avec cURL

```bash
curl -X GET \
  'http://localhost:3000/insurances/agency/INSURANCE_ID/subscribers' \
  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \
  -H 'Content-Type: application/json'
```

---

Une fois ce code implémenté et testé côté backend, relancez l'app Android et vous verrez les vraies données des utilisateurs ! 🚀
