import type { Player, Match } from "./types"

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/user";
const FLASK_API_URL = process.env.NEXT_PUBLIC_FLASK_API_URL || "http://localhost:5000";
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
  const data = await response.json();
  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error || "Player not found")
  }
  console.log(data.profile_picture)

  return {
    puuid: data.puuid,
    gameName: data.game_name,  // mapping game_name -> gameName
    tagLine: data.tag_line,    // mapping tag_line -> tagLine
    profilePicture: data.profile_picture,
  };
}

export async function getCachedMatches(puuid: string): Promise<Match[]> {
  const response = await fetch(`${API_BASE_URL}/matches/${puuid}`)

  if (!response.ok) {
    const error = await response.json()
    throw new Error(error.error || "Failed to fetch match history")
  }

  return response.json()
}

export async function analyzeMatch(match: Match){
  const response = await fetch(`${FLASK_API_URL}/analyze`, 
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        matchId: match.matchId,
        championName: match.championName,
        kills: match.kills,
        deaths: match.deaths,
        assists: match.assists,
        goldEarned: match.goldEarned,
        goldSpent: match.goldSpent,
        csPerMin: match.csPerMin,
        kda: match.kda,
        visionScore: match.visionScore,
        wardsPlaced: match.wardsPlaced,
        wardsKilled: match.wardsKilled,
        damageDealtToChampions: match.damageDealtToChampions,
        totalDamageTaken: match.totalDamageTaken,
        totalMinionsKilled: match.totalMinionsKilled,
        neutralMinionsKilled: match.neutralMinionsKilled,
        turretTakedowns: match.turretTakedowns,
        inhibitorTakedowns: match.inhibitorTakedowns,
        gameDuration: match.gameDuration,
        win: match.win,
        teamPosition: match.teamPosition,
      }),
    }
  );

  return response;
}