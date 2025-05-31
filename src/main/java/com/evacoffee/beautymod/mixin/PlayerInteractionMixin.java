@Mixin(PlayerEntity.class)
public abstract class PlayerInteractionMixin {
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void onInteract(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (entity instanceof PlayerEntity targetPlayer) {
            PlayerEntity thisPlayer = (PlayerEntity) (Object) this;

            // Check if players are in a relationship
            if (hand == Hand.MAIN_HAND && this Player.isSneaking()) {
                ActionResult result = ModEvents.Date_Start.invoker().interact(thisPlayer, targetPlayer);
                if (result != ActionResult.PASS) {
                    cir.setReturnValue(result);
                }
            }
        }
    }
}
