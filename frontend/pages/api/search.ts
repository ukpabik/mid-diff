import type { NextApiRequest, NextApiResponse } from "next";

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
  const { riotId, tagLine, platformRegion, routingRegion } = req.query;

  if (
    !riotId || !tagLine || !platformRegion || !routingRegion ||
    typeof riotId !== "string" || typeof tagLine !== "string" ||
    typeof platformRegion !== "string" || typeof routingRegion !== "string"
  ) {
    return res.status(400).json({ error: "Missing or invalid query parameters" });
  }

  const response = await fetch(`${process.env.INTERNAL_BACKEND_URL}/user/search/${riotId}/${tagLine}/${platformRegion}/${routingRegion}`, {
    headers: { "X-API-KEY": process.env.BACKEND_API_KEY as string },
  });

  const data = await response.json();
  res.status(response.status).json(data);
}
