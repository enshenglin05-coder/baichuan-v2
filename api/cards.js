// Vercel Serverless API Handler for Baichuan Cloud Cards API
// Target URL: https://baichuan-v2-uqih.vercel.app/api/cards

let cardStore = [];

export default function handler(req, res) {
  res.setHeader('Access-Control-Allow-Credentials', 'true');
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,OPTIONS,PATCH,DELETE,POST,PUT');
  res.setHeader(
    'Access-Control-Allow-Headers',
    'X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Api-Version, X-Baichuan-Version, X-Drawer-Matrix, X-AB-Direction, AB-Direction'
  );

  if (req.method === 'OPTIONS') {
    res.status(200).end();
    return;
  }

  const now = Date.now();
  const TWELVE_HOURS_MS = 12 * 60 * 60 * 1000;

  // Auto clean 12-hour TTL expired cards
  cardStore = cardStore.filter(card => {
    const cardTime = card.created_at || card.timestamp || now;
    return (now - cardTime) < TWELVE_HOURS_MS;
  });

  if (req.method === 'GET') {
    return res.status(200).json({
      success: true,
      code: 200,
      timestamp: now,
      total: cardStore.length,
      cards: cardStore
    });
  }

  if (req.method === 'POST' || req.method === 'PUT') {
    const rawBody = typeof req.body === 'string' ? JSON.parse(req.body) : req.body;
    if (!rawBody) {
      return res.status(400).json({
        success: false,
        code: 400,
        error: "Invalid payload: Body is required"
      });
    }

    const cardsToProcess = Array.isArray(rawBody) ? rawBody : [rawBody];
    const processedCards = [];

    for (const cardItem of cardsToProcess) {
      if (!cardItem || typeof cardItem !== 'object') continue;
      const cardId = cardItem.card_id || cardItem.cardId || cardItem.id;
      if (!cardId) continue;

      const timestampVal = cardItem.created_at || cardItem.timestamp || now;
      const categoryCode = cardItem.category_code || cardItem.drawer_code || "1-1";
      const drawerType = cardItem.drawer_type || cardItem.type || "1A";
      const lat = cardItem.gps_lat !== undefined ? cardItem.gps_lat : (cardItem.latitude || 0.0);
      const lng = cardItem.gps_lng !== undefined ? cardItem.gps_lng : (cardItem.longitude || 0.0);

      const cardWithTime = {
        ...cardItem,
        card_id: cardId,
        category_code: categoryCode,
        drawer_type: drawerType,
        gps_lat: lat,
        gps_lng: lng,
        created_at: timestampVal,
        timestamp: timestampVal,
        moderation_status: cardItem.moderation_status || "NORMAL"
      };

      const existingIndex = cardStore.findIndex(c => c.card_id === cardId);
      if (existingIndex !== -1) {
        cardStore[existingIndex] = cardWithTime;
      } else {
        cardStore.unshift(cardWithTime);
      }
      processedCards.push(cardWithTime);
    }

    return res.status(200).json({
      success: true,
      code: 200,
      message: `Successfully processed ${processedCards.length} card(s) with 200 OK`,
      total: cardStore.length,
      cards: processedCards
    });
  }

  if (req.method === 'DELETE') {
    const cardId = req.query.card_id || req.body?.card_id;
    if (cardId) {
      cardStore = cardStore.filter(c => c.card_id !== cardId);
      return res.status(200).json({
        success: true,
        code: 200,
        message: `Card ${cardId} deleted successfully`
      });
    }
    return res.status(400).json({ success: false, code: 400, error: "Missing card_id for deletion" });
  }

  return res.status(405).json({ success: false, code: 405, error: "Method not allowed" });
}
