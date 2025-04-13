import type { Player, Match } from "./types"

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/user";

export async function searchPlayer(riotId: string, tagLine: string): Promise<Player> {
  console.log(riotId, tagLine);
  const response = await fetch(`${API_BASE_URL}/search/${riotId}/${tagLine}`);

  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error || "Failed to find player")
  }

  return response.json()
}

export async function getPlayerFromDb(puuid: string): Promise<Player> {
  const response = await fetch(`${API_BASE_URL}/db/${puuid}`)

  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error || "Player not found")
  }

  return response.json()
}

export async function getCachedMatches(puuid: string): Promise<Match[]> {
  const response = await fetch(`${API_BASE_URL}/matches/${puuid}`)

  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error || "Failed to fetch match history")
  }

  return response.json()
}
