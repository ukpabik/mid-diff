export interface Player {
  puuid: string
  gameName: string
  tagLine: string
  profilePicture: string
}

export interface Match {
  matchId: string
  puuid: string
  championName: string
  championId: string
  role: string
  teamPosition: string
  win: boolean
  kills: number
  deaths: number
  assists: number
  goldEarned: number
  goldSpent: number
  totalMinionsKilled: number
  neutralMinionsKilled: number
  damageDealtToChampions: number
  totalDamageTaken: number
  visionScore: number
  wardsPlaced: number
  wardsKilled: number
  turretTakedowns: number
  inhibitorTakedowns: number
  gameStartTimestamp: number
  gameDuration: number
  gameMode: string
  queueId: number
  csPerMin: number
  kda: number
}

export interface RankInfoEntry {
  puuid: string;
  queueType: "RANKED_SOLO_5x5" | "RANKED_FLEX_SR";
  tier: string;
  player_rank: string;
  leaguePoints: number;
  wins: number;
  losses: number;
}

export interface RankInfo {
  solo: RankInfoEntry;
  flex: RankInfoEntry;
}

export interface ItemDto {
  id: number;
  name: string;
  description: string;
  totalGold: number;
}

export interface OptimalItemDto {
  id: number;
  name: string;
  description: string;
  totalGold: number;
  tags: string[];
  imageFileName: string;
}

export interface PlayerBuild {
  matchId: string;
  puuid: string;
  items: ItemDto[];
  ddragonVersion: string;
}

export interface AnalyzeMatchRequest {
  match: Match;
  build?: PlayerBuild;
  playerRank: string;
}

export interface AdviceResponse {
  label: string;
  description: string;
  archetype_description: string;
  advice: string;
}