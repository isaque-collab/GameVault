import type {GameSectionItem} from '../components/game/GameSection/GameSection.tsx'
import type {ItemColecaoJogo} from '../types/jogo.ts'

export function converterItemColecaoParaGameSectionItem(
    item: ItemColecaoJogo,
): GameSectionItem {
    return {
        id: item.rawgGameId,

        nome:
            item.metadadosDisponiveis && item.nome
                ? item.nome
                : `Jogo #${item.rawgGameId}`,

        imagemUrl:
            item.metadadosDisponiveis && item.imagemFundo
                ? item.imagemFundo
                : undefined,

        avaliacao:
            item.metadadosDisponiveis
                ? item.notaRawg ?? undefined
                : undefined,

        anoLancamento:
            item.metadadosDisponiveis && item.dataLancamento
                ? Number(item.dataLancamento.slice(0, 4))
                : undefined,
    }
}