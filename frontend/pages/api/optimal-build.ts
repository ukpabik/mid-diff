import type { NextApiRequest, NextApiResponse } from "next";
import type { ItemDto } from "@/lib/types";

export default async function handler(
  req: NextApiRequest,
  res: NextApiResponse<{ items: ItemDto[]; ddragonVersion: string } | { error: string }>
) {
  const champ = req.query.champion;
  if (typeof champ !== "string") {
    return res.status(400).json({ error: "Missing champion" });
  }

  try {
    const backendRes = await fetch(
      `${process.env.INTERNAL_BACKEND_URL}/user/optimal-build/${champ}`,
      { headers: { "X-API-KEY": process.env.BACKEND_API_KEY! } }
    );
    const data = await backendRes.json();
    return res.status(backendRes.status).json(data);
  } catch (e) {
    console.error("optimal-build proxy error", e);
    return res.status(500).json({ error: "Internal Server Error" });
  }
}
